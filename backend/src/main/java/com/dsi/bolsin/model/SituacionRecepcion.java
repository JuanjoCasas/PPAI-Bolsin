package com.dsi.bolsin.model;

import java.time.LocalDateTime;

/**
 * Situación de la documentación que el EB marca en los flujos alternativos del CU 28
 * (regla de negocio "Recepción de un Bolsín en una CM Destino").
 *
 * Cada situación sabe a qué transición de la máquina de estados corresponde y si deja
 * al remito como "Recibido y aceptado parcial". La documentación no marcada se acepta.
 */
public enum SituacionRecepcion {

    // A3: la documentación que no llegó queda como no recibida; el remito, recibido y aceptado parcial
    NO_RECIBIDA(true) {
        @Override
        public void registrar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable) {
            documentacion.docNoRecibida(fechaHoraActual, responsable);
        }
    },

    // A4: la documentación que no corresponde a la CM destino se marca como recibida y rechazada
    NO_CORRESPONDE_DESTINO(true) {
        @Override
        public void registrar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable) {
            documentacion.recibirYRechazar(fechaHoraActual, responsable);
        }
    },

    // A5: la CMC recibe documentación que se debe redirigir a un área de la sede central
    PARA_REDIRIGIR(false) {
        @Override
        public void registrar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable) {
            documentacion.redirigir(fechaHoraActual, responsable);
        }
    };

    private final boolean dejaRemitoParcial;

    SituacionRecepcion(boolean dejaRemitoParcial) {
        this.dejaRemitoParcial = dejaRemitoParcial;
    }

    public abstract void registrar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable);

    public boolean dejaRemitoParcial() {
        return dejaRemitoParcial;
    }
}
