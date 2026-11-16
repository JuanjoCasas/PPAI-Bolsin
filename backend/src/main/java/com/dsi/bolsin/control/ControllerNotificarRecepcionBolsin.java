package com.dsi.bolsin.control;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.dsi.bolsin.boundary.InterfazServidorCorreo;
import com.dsi.bolsin.dto.NotificacionResponse;
import com.dsi.bolsin.model.Bolsin;
import com.dsi.bolsin.model.ComisionMedica;
import com.dsi.bolsin.model.Documentacion;

/**
 * <<control>> del CU 29 Notificar recepción de bolsín.
 *
 * Lo incluye el CU 28 (<<include>>): no lo dispara un actor, sino el controller del CU 28.
 * Por eso no tiene pantalla; su única boundary es la interfaz con el Servidor de Correo.
 *
 * ERS: "Notificar por email a la comisión médica origen la recepción del bolsín y la situación
 * de la documentación correspondiente a la misma".
 */
@Service
public class ControllerNotificarRecepcionBolsin {

    private final InterfazServidorCorreo interfazServidorCorreo;

    public ControllerNotificarRecepcionBolsin(InterfazServidorCorreo interfazServidorCorreo) {
        this.interfazServidorCorreo = interfazServidorCorreo;
    }

    // ControllerRegistrarRecepcionBolsin -> Control CU 29 : notificarRecepcionBolsin(bolsin)
    public List<NotificacionResponse> notificarRecepcionBolsin(Bolsin bolsin) {
        Map<ComisionMedica, List<Documentacion>> documentacionPorCM = agruparDocumentacionPorCMOrigen(bolsin);

        List<NotificacionResponse> notificaciones = new ArrayList<>();
        // Loop: para cada CM de origen que envió documentación -> enviarMail()
        for (Map.Entry<ComisionMedica, List<Documentacion>> entrada : documentacionPorCM.entrySet()) {
            ComisionMedica cmOrigen = entrada.getKey();
            List<Documentacion> documentaciones = entrada.getValue();

            String resultado = interfazServidorCorreo.enviarMail(cmOrigen.getEmail(),
                    "Recepción del bolsín precinto N° " + bolsin.getNroPrecinto(),
                    armarCuerpo(bolsin, cmOrigen, documentaciones));

            notificaciones.add(new NotificacionResponse(cmOrigen.getNombre(), cmOrigen.getEmail(),
                    documentaciones.size(), resultado));
        }
        return notificaciones;
    }

    // Loop: para cada documentación del bolsín -> getCMOrigen()
    private Map<ComisionMedica, List<Documentacion>> agruparDocumentacionPorCMOrigen(Bolsin bolsin) {
        // Se agrupa por código de CM (ComisionMedica no define equals)
        Map<Integer, ComisionMedica> cms = new LinkedHashMap<>();
        Map<Integer, List<Documentacion>> docs = new LinkedHashMap<>();
        for (Documentacion doc : bolsin.getDocumentacion()) {
            ComisionMedica cm = doc.getCMOrigen();
            cms.putIfAbsent(cm.getCodigo(), cm);
            docs.computeIfAbsent(cm.getCodigo(), c -> new ArrayList<>()).add(doc);
        }
        Map<ComisionMedica, List<Documentacion>> porCM = new LinkedHashMap<>();
        cms.forEach((codigo, cm) -> porCM.put(cm, docs.get(codigo)));
        return porCM;
    }

    // Situación de la documentación (Obs. 2): número, asunto y estado en que quedó
    private String armarCuerpo(Bolsin bolsin, ComisionMedica cmOrigen, List<Documentacion> documentaciones) {
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Sres. ").append(cmOrigen.getNombre()).append(":\n")
                .append("Se registró la recepción del bolsín precinto N° ").append(bolsin.getNroPrecinto())
                .append(" en ").append(bolsin.getCMDestino()).append(".\n")
                .append("Situación de la documentación enviada:\n");
        for (Documentacion doc : documentaciones) {
            String[] datos = doc.getDatosDocumentacion();
            cuerpo.append(" - N° ").append(datos[0]).append(" ").append(datos[1])
                    .append(": ").append(doc.getEstadoActual().getNombre())
                    .append(" (").append(doc.getEstadoActual().getDescripcion()).append(")\n");
        }
        return cuerpo.toString();
    }
}
