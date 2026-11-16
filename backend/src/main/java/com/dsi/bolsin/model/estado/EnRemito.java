package com.dsi.bolsin.model.estado;

public class EnRemito extends EstadoDocumentacion {

    public static final String NOMBRE = "EnRemito";

    public EnRemito() {
        super(NOMBRE, "La documentación fue incluida en un remito");
    }

    @Override
    public void cancelarRemito() {
        throw fueraDeAlcance("cancelarRemito");
    }

    @Override
    public void agregarABolsin() {
        throw fueraDeAlcance("agregarABolsin");
    }
}
