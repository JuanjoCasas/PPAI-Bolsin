package com.dsi.bolsin.model.estado;

public class EnBolsinSaliente extends EstadoDocumentacion {

    public static final String NOMBRE = "EnBolsinSaliente";

    public EnBolsinSaliente() {
        super(NOMBRE, "El remito de la documentación está en un bolsín por enviar");
    }

    @Override
    public void modificarBolsin() {
        throw fueraDeAlcance("modificarBolsin");
    }

    @Override
    public void quitarDeBolsin() {
        throw fueraDeAlcance("quitarDeBolsin");
    }

    @Override
    public void retirarBolsin() {
        throw fueraDeAlcance("retirarBolsin");
    }
}
