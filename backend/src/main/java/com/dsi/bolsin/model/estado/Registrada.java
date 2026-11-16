package com.dsi.bolsin.model.estado;

public class Registrada extends EstadoDocumentacion {

    public static final String NOMBRE = "Registrada";

    public Registrada() {
        super(NOMBRE, "La documentación fue registrada");
    }

    @Override
    public void incluirEnRemito() {
        throw fueraDeAlcance("incluirEnRemito");
    }

    @Override
    public void eliminarDocumentacion() {
        throw fueraDeAlcance("eliminarDocumentacion");
    }
}
