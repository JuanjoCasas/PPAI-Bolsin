package com.dsi.bolsin.model;

import java.time.LocalDateTime;

import com.dsi.bolsin.model.estado.EstadoDocumentacion;
import com.dsi.bolsin.model.estado.EstadoDocumentacionConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cambios_estado_documentacion")
public class CambioEstadoDocumentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column
    private LocalDateTime fechaHoraFin;

    // CambioEstadoDocumentacion -> EstadoDocumentacion (1) : estado
    @Convert(converter = EstadoDocumentacionConverter.class)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoDocumentacion estado;

    // CambioEstadoDocumentacion -> Empleado (1) : responsableCE
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsable_email", nullable = false)
    private Empleado responsableCE;

    protected CambioEstadoDocumentacion() {
    }

    // new(fechaHoraInicio, estado, responsable)
    public CambioEstadoDocumentacion(LocalDateTime fechaHoraInicio, EstadoDocumentacion estado, Empleado responsableCE) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
        this.responsableCE = responsableCE;
    }

    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public EstadoDocumentacion getEstado() {
        return estado;
    }
}
