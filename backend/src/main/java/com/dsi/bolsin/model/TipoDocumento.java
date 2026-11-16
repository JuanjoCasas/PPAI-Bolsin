package com.dsi.bolsin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tipos_documento")
public class TipoDocumento {

    @Id
    @Column(length = 100)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    protected TipoDocumento() {
    }

    public TipoDocumento(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
