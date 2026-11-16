package com.dsi.bolsin.dto;

/**
 * CM origen a la que el CU 29 le notificó la recepción del bolsín.
 * resultadoEnvio: "Enviado", "Simulado (...)" o "Error al enviar: ...".
 */
public record NotificacionResponse(String cmOrigen, String email, int cantidadDocumentaciones,
        String resultadoEnvio) {
}
