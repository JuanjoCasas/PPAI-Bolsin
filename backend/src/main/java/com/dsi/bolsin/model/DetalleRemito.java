package com.dsi.bolsin.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "detalles_remito")
public class DetalleRemito {

    @Id
    private Integer id;

    @Column(name = "area_cmc_destino", length = 100)
    private String areaCMCDestino;

    // DetalleRemito -> Documentacion (1) : documentacion
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "documentacion_numero", nullable = false)
    private Documentacion documentacion;

    protected DetalleRemito() {
    }

    // Devuelve [numero, asunto, tipo] de la documentación
    public String[] mostrarDocumentacion() {
        String[] datos = documentacion.getDatosDocumentacion();
        String tipo = documentacion.getTipoDocumentacion();
        return new String[] { datos[0], datos[1], tipo };
    }

    // DetalleRemito -> Documentacion : aceptar(fechaHoraActual, responsable)
    public void actualizarEstadoDoc(LocalDateTime fechaHoraActual, Empleado responsable) {
        documentacion.aceptar(fechaHoraActual, responsable);
    }

    // Flujos alternativos A3, A4 y A5: la situación decide la transición de la documentación
    public void registrarSituacionDoc(SituacionRecepcion situacion, LocalDateTime fechaHoraActual,
            Empleado responsable) {
        situacion.registrar(documentacion, fechaHoraActual, responsable);
    }

    public boolean esDocumentacion(Integer numeroDocumentacion) {
        return documentacion.getNumero().equals(numeroDocumentacion);
    }

    public Documentacion getDocumentacion() {
        return documentacion;
    }
}
