package com.dsi.bolsin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "comisiones_medicas")
public class ComisionMedica {

    @Id
    private Integer codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String direccion;

    @Column(length = 100)
    private String email;

    @Column(length = 30)
    private String telefono;

    protected ComisionMedica() {
    }

    public ComisionMedica(Integer codigo, String nombre, String direccion, String email, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    // CU 29: dirección a la que se notifica la recepción del bolsín
    public String getEmail() {
        return email;
    }
}
