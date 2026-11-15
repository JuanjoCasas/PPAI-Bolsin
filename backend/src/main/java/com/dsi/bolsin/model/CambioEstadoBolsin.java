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
@Table(name = "cambios_estado_bolsin")
public class CambioEstadoBolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column
    private LocalDateTime fechaHoraFin;

    // CambioEstadoBolsin -> Estado (1) : estado
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    // CambioEstadoBolsin -> Empleado (1) : responsableCE
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsable_email", nullable = false)
    private Empleado responsableCE;

    protected CambioEstadoBolsin() {
    }

    // new()
    public CambioEstadoBolsin(LocalDateTime fechaHoraInicio, Estado estado, Empleado responsableCE) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.responsableCE = responsableCE;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    // actual:CambioEstadoBolsin -> Estado : esEnviado()
    public boolean sosEnviado() {
        return estado.esEnviado();
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
}
