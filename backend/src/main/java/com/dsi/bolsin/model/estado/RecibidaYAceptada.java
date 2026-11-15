package com.dsi.bolsin.model.estado;

public class RecibidaYAceptada extends EstadoDocumentacion {

    public static final String NOMBRE = "RecibidaYAceptada";

    public RecibidaYAceptada() {
        super(NOMBRE, "La documentación fue recibida y aceptada en la CM destino (estado final)");
    }
}
