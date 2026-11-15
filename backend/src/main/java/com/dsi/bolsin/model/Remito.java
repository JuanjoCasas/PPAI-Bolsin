package com.dsi.bolsin.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "remitos")
public class Remito {

    @Id
    private Integer numero;

    @Column(nullable = false)
    private LocalDate fecha;

    // Remito -> ComisionMedica (1) : origen
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cm_origen_codigo", nullable = false)
    private ComisionMedica origen;

    // Remito -> ComisionMedica (1) : destino
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cm_destino_codigo", nullable = false)
    private ComisionMedica destino;

    // Remito -> Estado (1) : estado
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id", nullable = false)
    private Estado estado;

    // Remito o--> DetalleRemito (1..*) : detalleRemito
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "remito_numero", nullable = false)
    private List<DetalleRemito> detalleRemito = new ArrayList<>();

    protected Remito() {
    }

    /**
     * Devuelve una fila por cada documentación incluida en el remito:
     * [numeroRemito, numeroDocumentacion, asunto, tipoDocumento]
     */
    public List<String[]> getDatosRemito() {
        String numeroRemito = String.valueOf(getNumero());
        List<String[]> filas = new ArrayList<>();
        for (String[] doc : mostrarDocumentacionIncluida()) {
            filas.add(new String[] { numeroRemito, doc[0], doc[1], doc[2] });
        }
        return filas;
    }

    public Integer getNumero() {
        return numero;
    }

    // Loop: para todos los detalles del remito -> mostrarDocumentacion()
    private List<String[]> mostrarDocumentacionIncluida() {
        List<String[]> documentos = new ArrayList<>();
        for (DetalleRemito detalle : detalleRemito) {
            documentos.add(detalle.mostrarDocumentacion());
        }
        return documentos;
    }

    public void setEstado(Estado estado, LocalDateTime fechaHoraActual, Empleado responsable) {
        this.estado = estado;
        actualizarEstadoDoc(fechaHoraActual, responsable);
    }

    // Loop: para cada documentación del remito -> actualizarEstadoDoc()
    private void actualizarEstadoDoc(LocalDateTime fechaHoraActual, Empleado responsable) {
        for (DetalleRemito detalle : detalleRemito) {
            detalle.actualizarEstadoDoc(fechaHoraActual, responsable);
        }
    }

    public Estado getEstado() {
        return estado;
    }

    public List<DetalleRemito> getDetalleRemito() {
        return detalleRemito;
    }
}
