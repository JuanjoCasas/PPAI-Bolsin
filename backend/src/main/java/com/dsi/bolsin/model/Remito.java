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

    // Flujo básico (diagrama de secuencia de diseño)
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

    /**
     * Flujos alternativos (A3, A4, A5): la documentación marcada por el EB toma la situación
     * indicada y el resto se acepta. Si la situación lo requiere (A3, A4) y el remito tiene
     * documentación marcada, queda en estadoParcial en lugar de estado.
     */
    public void setEstadoConSituacion(Estado estado, Estado estadoParcial, List<Integer> documentacionMarcada,
            SituacionRecepcion situacion, LocalDateTime fechaHoraActual, Empleado responsable) {
        boolean tieneDocumentacionMarcada = actualizarEstadoDocConSituacion(documentacionMarcada, situacion,
                fechaHoraActual, responsable);
        this.estado = tieneDocumentacionMarcada && situacion.dejaRemitoParcial() ? estadoParcial : estado;
    }

    // Loop: para cada documentación del remito -> actualizarEstadoDoc() / registrarSituacionDoc()
    private boolean actualizarEstadoDocConSituacion(List<Integer> documentacionMarcada, SituacionRecepcion situacion,
            LocalDateTime fechaHoraActual, Empleado responsable) {
        boolean tieneDocumentacionMarcada = false;
        for (DetalleRemito detalle : detalleRemito) {
            if (documentacionMarcada.stream().anyMatch(detalle::esDocumentacion)) {
                detalle.registrarSituacionDoc(situacion, fechaHoraActual, responsable);
                tieneDocumentacionMarcada = true;
            } else {
                detalle.actualizarEstadoDoc(fechaHoraActual, responsable);
            }
        }
        return tieneDocumentacionMarcada;
    }

    public Estado getEstado() {
        return estado;
    }

    public List<DetalleRemito> getDetalleRemito() {
        return detalleRemito;
    }
}
