package com.dsi.bolsin.model.estado;

public class NoRecibida extends EstadoDocumentacion {

    public static final String NOMBRE = "NoRecibida";

    public NoRecibida() {
        super(NOMBRE, "La documentación no llegó en el bolsín");
    }

    @Override
    public void reenviarDocumentacion() {
        throw fueraDeAlcance("reenviarDocumentacion");
    }

    @Override
    public void eliminarDocumentacion() {
        throw fueraDeAlcance("eliminarDocumentacion");
    }
}
