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
import org.springframework.jdbc.core.JdbcTemplate;

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
import com.dsi.bolsin.model.estado.NoRecibida;
import com.dsi.bolsin.model.estado.ParaRedirigir;
import com.dsi.bolsin.model.estado.RecibidaYAceptada;
import com.dsi.bolsin.model.estado.RecibidaYRechazada;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

        // La opción 1 no pide marcar documentación
        assertFalse(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_CONTENIDO_IGUAL));

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

    @Test
    void a3_noSeRecibeTodaLaDocumentacion() {
        controller.opcionRegistrarBolsin();
        controller.tomarSeleccionBolsin(50006);
        assertTrue(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_NO_SE_RECIBE_TODA));
        controller.tomarSeleccionDocumentacion(List.of(3009));
        assertTrue(controller.pedirConfirmacion().contains("3009"));

        List<NotificacionResponse> notificaciones = controller.confirmarRecepcion();
        assertEquals(1, notificaciones.size());
        InterfazServidorCorreo.Mail mail = ultimoMail();
        assertTrue(mail.cuerpo().contains("3009 Estudio de espirometría trabajador 30.222.111: " + NoRecibida.NOMBRE));

        Bolsin bolsin = bolsinRepository.findById(1006).orElseThrow();
        assertFalse(bolsin.sosEnviado());
        assertInstanceOf(NoRecibida.class, documentacion(bolsin, 3009).getEstadoActual());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(bolsin, 3008).getEstadoActual());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(bolsin, 3010).getEstadoActual());
        // El remito con documentación no recibida queda parcial; el otro, aceptado
        assertTrue(remito(bolsin, 2007).getEstado().esRecibidoYAceptadoParcial());
        assertTrue(remito(bolsin, 2008).getEstado().esRecibidoYAceptado());
    }

    @Test
    void a4_documentacionQueNoCorrespondeAlDestino() {
        controller.opcionRegistrarBolsin();
        controller.tomarSeleccionBolsin(50007);
        assertTrue(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_NO_CORRESPONDE_DESTINO));
        controller.tomarSeleccionDocumentacion(List.of(3012));
        controller.confirmarRecepcion();

        Bolsin bolsin = bolsinRepository.findById(1007).orElseThrow();
        Documentacion rechazada = documentacion(bolsin, 3012);
        assertInstanceOf(RecibidaYRechazada.class, rechazada.getEstadoActual());
        assertEquals(1, rechazada.getCambioEstado().stream().filter(CambioEstadoDocumentacion::sosActual).count());
        assertInstanceOf(RecibidaYAceptada.class, documentacion(bolsin, 3011).getEstadoActual());
        assertTrue(remito(bolsin, 2009).getEstado().esRecibidoYAceptadoParcial());
    }

    @Test
    void a5_redirigirSoloLoPuedeHacerLaCMC() {
        // Córdoba no es la CMC: no puede elegir redirigir
        controller.opcionRegistrarBolsin();
        controller.tomarSeleccionBolsin(50002);
        assertThrows(IllegalArgumentException.class,
                () -> controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_REDIRIGIR));

        // Se loguea el EB de la CMC
        jdbcTemplate.update("UPDATE sesiones SET fecha_fin = CURRENT_TIMESTAMP WHERE id = 2");
        jdbcTemplate.update("UPDATE sesiones SET fecha_fin = NULL WHERE id = 3");
        try {
            InicioResponse inicio = controller.opcionRegistrarBolsin();
            assertEquals("Comisión Médica Central", inicio.cmUsuario());
            controller.tomarSeleccionBolsin(50008);
            assertTrue(controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_REDIRIGIR));
            controller.tomarSeleccionDocumentacion(List.of(3013));
            controller.confirmarRecepcion();

            Bolsin bolsin = bolsinRepository.findById(1008).orElseThrow();
            assertInstanceOf(ParaRedirigir.class, documentacion(bolsin, 3013).getEstadoActual());
            assertInstanceOf(RecibidaYAceptada.class, documentacion(bolsin, 3014).getEstadoActual());
            // Redirigir no deja al remito parcial: la documentación llegó bien a la CMC
            assertTrue(remito(bolsin, 2010).getEstado().esRecibidoYAceptado());
        } finally {
            jdbcTemplate.update("UPDATE sesiones SET fecha_fin = CURRENT_TIMESTAMP WHERE id = 3");
            jdbcTemplate.update("UPDATE sesiones SET fecha_fin = NULL WHERE id = 2");
        }
    }

    @Test
    void flujoAlternativo_validaLaDocumentacionMarcada() {
        controller.opcionRegistrarBolsin();
        controller.tomarSeleccionBolsin(50002);
        controller.tomarOpcionRecepcion(ControllerRegistrarRecepcionBolsin.OPCION_NO_SE_RECIBE_TODA);

        assertThrows(IllegalArgumentException.class, () -> controller.tomarSeleccionDocumentacion(List.of()));
        assertThrows(IllegalArgumentException.class, () -> controller.tomarSeleccionDocumentacion(List.of(3001)),
                "La 3001 no está en el bolsín 50002");
        // Sin documentación marcada no se puede confirmar
        assertThrows(IllegalStateException.class, () -> controller.confirmarRecepcion());
        assertTrue(bolsinRepository.findById(1002).orElseThrow().sosEnviado());
    }

    private InterfazServidorCorreo.Mail ultimoMail() {
        List<InterfazServidorCorreo.Mail> mails = interfazServidorCorreo.getEnviados();
        return mails.get(mails.size() - 1);
    }

    private static Remito remito(Bolsin bolsin, int numero) {
        return bolsin.getRemito().stream().filter(r -> r.getNumero() == numero).findFirst().orElseThrow();
    }

    private static Documentacion documentacion(Bolsin bolsin, int numero) {
        return bolsin.getDocumentacion().stream().filter(d -> d.getNumero() == numero).findFirst().orElseThrow();
    }
}
