package com.dsi.bolsin.model.estado;

/**
 * Se lanza cuando se pide a la documentación una transición que no es válida
 * desde su estado actual (implementación por defecto de {@link EstadoDocumentacion}).
 */
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
