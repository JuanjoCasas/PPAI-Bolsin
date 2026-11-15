package com.dsi.bolsin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.dsi.bolsin.boundary.InterfazServidorCorreo;
import com.dsi.bolsin.control.ControllerRegistrarRecepcionBolsin;
import com.dsi.bolsin.dto.NotificacionResponse;
import com.dsi.bolsin.dto.BolsinResponse;
import com.dsi.bolsin.dto.DetalleBolsinResponse;
import com.dsi.bolsin.dto.InicioResponse;
import com.dsi.bolsin.model.Bolsin;
import com.dsi.bolsin.model.CambioEstadoDocumentacion;
import com.dsi.bolsin.model.DetalleRemito;
import com.dsi.bolsin.model.Documentacion;
import com.dsi.bolsin.model.Remito;
import com.dsi.bolsin.model.estado.EnBolsinSaliente;
import com.dsi.bolsin.model.estado.RecibidaYAceptada;
import com.dsi.bolsin.model.estado.TransicionInvalidaException;
import com.dsi.bolsin.repository.IBolsinRepository;

// Los tests nunca mandan mails reales, aunque exista backend/mail-local.properties
@SpringBootTest(properties = "notificaciones.mail.habilitado=false")
class RegistrarRecepcionBolsinTest {

    @Autowired
    private ControllerRegistrarRecepcionBolsin controller;

    @Autowired
    private IBolsinRepository bolsinRepository;

    @Autowired
    private InterfazServidorCorreo interfazServidorCorreo;

    @Test
    void flujoBasico_aceptaLaDocumentacionDelBolsin() {
        InicioResponse inicio = controller.opcionRegistrarBolsin();
        assertEquals("Comisión Médica Córdoba", inicio.cmUsuario());
        List<Integer> precintos = inicio.bolsines().stream().map(BolsinResponse::numeroPrecinto).toList();
        assertTrue(precintos.contains(50001));
        assertFalse(precintos.contains(50004), "Destino Rosario: no es de la CM del usuario");
        assertFalse(precintos.contains(50005), "Estado Cerrado: todavía no fue enviado");

        DetalleBolsinResponse detalle = controller.tomarSeleccionBolsin(50001);
        assertNotNull(detalle);
        assertEquals(2, detalle.remitos().size());
        assertEquals(3, detalle.remitos().stream().mapToInt(r -> r.documentaciones().size()).sum());

        // Paso 7: las 4 opciones de recepción de la ERS
        assertEquals(List.of(
                ControllerRegistrarRecepcionBolsin.OPCION_CONTENIDO_IGUAL,
                ControllerRegistrarRecepcionBolsin.OPCION_NO_SE_RECIBE_TODA,
                ControllerRegistrarRecepcionBolsin.OPCION_NO_CORRESPONDE_DESTINO,
                ControllerRegistrarRecepcionBolsin.OPCION_REDIRIGIR), detalle.opcionesRecepcion());

        // A3, A4 y A5 (flujos alternativos) no están implementados
        assertFalse(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_NO_SE_RECIBE_TODA));
        assertFalse(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_NO_CORRESPONDE_DESTINO));
        assertFalse(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_REDIRIGIR));
        assertTrue(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_CONTENIDO_IGUAL));

        int mailsAntes = interfazServidorCorreo.getEnviados().size();
        List<NotificacionResponse> notificaciones = controller.confirmarRecepcion();

        // <<include>> CU 29: un mail a la CM origen de la documentación (Rosario), con sus 3 documentaciones
        assertEquals(List.of(new NotificacionResponse("Comisión Médica Rosario", "ppaibolsines+rosario@gmail.com", 3,
                InterfazServidorCorreo.SIMULADO)),
                notificaciones);
        List<InterfazServidorCorreo.Mail> mails = interfazServidorCorreo.getEnviados();
        assertEquals(mailsAntes + 1, mails.size());
        InterfazServidorCorreo.Mail mail = mails.get(mails.size() - 1);
        assertEquals("ppaibolsines+rosario@gmail.com", mail.destinatario());
        assertTrue(mail.cuerpo().contains("3001"));
        assertTrue(mail.cuerpo().contains("3003"));

        Bolsin bolsin = bolsinRepository.findById(1001).orElseThrow();
        assertFalse(bolsin.sosEnviado(), "El bolsín ya no está Enviado");
        for (Remito remito : bolsin.getRemito()) {
            assertTrue(remito.getEstado().esRecibidoYAceptado());
            for (DetalleRemito detalleRemito : remito.getDetalleRemito()) {
                Documentacion doc = detalleRemito.getDocumentacion();
                assertInstanceOf(RecibidaYAceptada.class, doc.getEstadoActual());

                List<CambioEstadoDocumentacion> actuales = doc.getCambioEstado().stream()
                        .filter(CambioEstadoDocumentacion::sosActual).toList();
                assertEquals(1, actuales.size(), "Un solo cambio de estado actual");
                assertInstanceOf(RecibidaYAceptada.class, actuales.get(0).getEstado());
                assertEquals(3, doc.getCambioEstado().size());
            }
        }

        // Ya recibido: no vuelve a aparecer entre los bolsines entrantes
        List<Integer> despues = controller.opcionRegistrarBolsin().bolsines().stream()
                .map(BolsinResponse::numeroPrecinto).toList();
        assertFalse(despues.contains(50001));
    }

    @Test
    void documentacionEnEstadoInvalido_rechazaLaTransicionYHaceRollback() {
        controller.opcionRegistrarBolsin();
        controller.tomarSeleccionBolsin(50003);
        controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_CONTENIDO_IGUAL);

        int mailsAntes = interfazServidorCorreo.getEnviados().size();
        assertThrows(TransicionInvalidaException.class, () -> controller.confirmarRecepcion());
        assertEquals(mailsAntes, interfazServidorCorreo.getEnviados().size(), "Con rollback no se notifica");

        Bolsin bolsin = bolsinRepository.findById(1003).orElseThrow();
        assertTrue(bolsin.sosEnviado(), "Rollback: el bolsín sigue Enviado");
        Documentacion doc = bolsin.getRemito().get(0).getDetalleRemito().get(0).getDocumentacion();
        assertInstanceOf(EnBolsinSaliente.class, doc.getEstadoActual());
        assertEquals(1, doc.getCambioEstado().size());
    }
}
