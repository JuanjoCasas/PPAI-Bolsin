package com.dsi.bolsin.model.estado;

public class RecibidaYRechazada extends EstadoDocumentacion {

    public static final String NOMBRE = "RecibidaYRechazada";

    public RecibidaYRechazada() {
        super(NOMBRE, "La documentación llegó pero no corresponde a la CM destino");
    }

    @Override
    public void reenviarDocumentacion() {
        throw fueraDeAlcance("reenviarDocumentacion");
    }
}
