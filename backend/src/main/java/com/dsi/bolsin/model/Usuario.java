package com.dsi.bolsin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(length = 50)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String contrasena;

    // Usuario -> Empleado (1) : empleado
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empleado_email", nullable = false)
    private Empleado empleado;

    protected Usuario() {
    }

    public Usuario(String nombre, String contrasena, Empleado empleado) {
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.empleado = empleado;
    }

    // Usuario -> Empleado : getEmpleado()
    public Empleado getEmpleado() {
        return empleado;
    }
}
