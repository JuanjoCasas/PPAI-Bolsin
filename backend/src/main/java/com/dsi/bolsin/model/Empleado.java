package com.dsi.bolsin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    // Empleado -> ComisionMedica (1) : asignadoA
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cm_codigo", nullable = false)
    private ComisionMedica asignadoA;

    // Empleado -> Rol (1) : rol
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_nombre", nullable = false)
    private Rol rol;

    protected Empleado() {
    }

    public Empleado(String email, String nombre, String apellido, ComisionMedica asignadoA, Rol rol) {
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
        this.asignadoA = asignadoA;
        this.rol = rol;
    }

    // Empleado -> ComisionMedica : getCM()
    public ComisionMedica getCM() {
        return asignadoA;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }
}
