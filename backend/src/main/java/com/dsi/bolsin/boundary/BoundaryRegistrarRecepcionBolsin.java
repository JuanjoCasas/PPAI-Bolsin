package com.dsi.bolsin.boundary;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dsi.bolsin.control.ControllerRegistrarRecepcionBolsin;
import com.dsi.bolsin.dto.DetalleBolsinResponse;
import com.dsi.bolsin.dto.InicioResponse;
import com.dsi.bolsin.dto.NotificacionResponse;
import com.dsi.bolsin.dto.SeleccionarBolsinRequest;
import com.dsi.bolsin.dto.SeleccionarOpcionRequest;
import com.dsi.bolsin.model.estado.TransicionInvalidaException;

/**
 * <<boundary>> del CU 28. Cada mensaje del actor (EB) a la pantalla es un endpoint.
 * Los mensajes mostrarX() / pedirConfirmacion() / informarEjecucionExitosa() del diagrama
 * se resuelven con la respuesta HTTP, que el front (React) dibuja.
 */
@RestController
@RequestMapping("/bolsines")
public class BoundaryRegistrarRecepcionBolsin {

    private final ControllerRegistrarRecepcionBolsin controller;

    public BoundaryRegistrarRecepcionBolsin(ControllerRegistrarRecepcionBolsin controller) {
        this.controller = controller;
    }

    
    @PostMapping("/iniciar")
    public ResponseEntity<?> opcionRegistrarBolsin() {
        try {
            return ResponseEntity.ok(habilitarPantalla());
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    // Boundary -> Control : opcionRegistrarBolsin()
    // Devuelve mostrarCMUsuario() + mostrarBolsines()
    private InicioResponse habilitarPantalla() {
        return controller.opcionRegistrarBolsin();
    }

    
    @PostMapping("/seleccionar-bolsin")
    public ResponseEntity<?> seleccionarBolsin(@RequestBody SeleccionarBolsinRequest request) {
        DetalleBolsinResponse detalle = controller.tomarSeleccionBolsin(request.numeroPrecinto());
        if (detalle == null) {
            return error(HttpStatus.NOT_FOUND, "No se encontró el bolsín seleccionado entre los bolsines entrantes");
        }
        // mostrarDetalleBolsin() + mostrarOpcionesRecepcionBolsin()
        return ResponseEntity.ok(detalle);
    }

    // EB -> Boundary : seleccionaOpcionContenidoCoincide()
    @PostMapping("/seleccionar-opcion")
    public ResponseEntity<?> seleccionaOpcionContenidoCoincide(@RequestBody SeleccionarOpcionRequest request) {
        boolean opcionValida = controller.tomarOpcionRecepcion(request.opcion());
        if (!opcionValida) {
            return error(HttpStatus.BAD_REQUEST,
                    "Esta opción corresponde a un flujo alternativo que no está implementado. Solo se implementa: \""
                            + ControllerRegistrarRecepcionBolsin.OPCION_CONTENIDO_IGUAL + "\"");
        }
        // Control -> Boundary : pedirConfirmacion()
        return ResponseEntity.ok(Map.of("mensaje", "¿Confirma la recepción del bolsín?"));
    }

    // EB -> Boundary : confirmar()
    @PostMapping("/confirmar")
    public ResponseEntity<?> confirmar() {
        List<NotificacionResponse> notificaciones;
        try {
            notificaciones = controller.confirmarRecepcion();
        } catch (TransicionInvalidaException | UnsupportedOperationException | IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        }
        // Control -> Boundary : informarEjecucionExitosa()
        return ResponseEntity.ok(Map.of(
                "mensaje", "La recepción del bolsín se registró con éxito",
                "notificaciones", notificaciones));
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("error", mensaje));
    }
}
