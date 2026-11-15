package com.dsi.bolsin.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sesiones")
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column
    private LocalDateTime fechaFin;

    // Sesion -> Usuario (1) : usuario
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_nombre", nullable = false)
    private Usuario usuario;

    protected Sesion() {
    }

    // No figura en los diagramas: el control necesita encontrar la sesión activa
    // (la que no tiene fecha de fin). En el diagrama vamos directo a "activa:Sesion".
    public boolean sosActiva() {
        return fechaFin == null;
    }

    // Sesion -> Usuario : getEmpleado() -> Empleado : getCM()
    public ComisionMedica getCMUsuarioLogueado() {
        return usuario.getEmpleado().getCM();
    }

    // No figura en los diagramas: el control necesita al empleado logueado
    // (atributo empleadoLogueado) para registrarlo como responsable del cambio de estado.
    public Empleado getEmpleadoLogueado() {
        return usuario.getEmpleado();
    }
}
