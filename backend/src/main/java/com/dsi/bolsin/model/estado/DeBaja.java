package com.dsi.bolsin.model.estado;

public class DeBaja extends EstadoDocumentacion {

    public static final String NOMBRE = "DeBaja";

    public DeBaja() {
        super(NOMBRE, "La documentación fue dada de baja (estado final)");
    }
}
