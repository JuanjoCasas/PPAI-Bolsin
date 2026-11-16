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
@Table(name = "bolsines")
public class Bolsin {

    @Id
    private Integer numeroBolsin;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Integer numeroPrecinto;

    @Column
    private Float peso;

    // Bolsin -> ComisionMedica (1) : origen
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cm_origen_codigo", nullable = false)
    private ComisionMedica origen;

    // Bolsin -> ComisionMedica (1) : destino
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cm_destino_codigo", nullable = false)
    private ComisionMedica destino;

    // Bolsin o--> Remito (1..*) : remito
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "bolsin_numero", nullable = false)
    private List<Remito> remito = new ArrayList<>();

    // Bolsin -> CambioEstadoBolsin (1..*) : cambioEstado
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "bolsin_numero", nullable = false)
    private List<CambioEstadoBolsin> cambioEstado = new ArrayList<>();

    protected Bolsin() {
    }

    public boolean esTuCMDestino(ComisionMedica cm) {
        return destino.getCodigo().equals(cm.getCodigo());
    }

    // Loop "* sosActual()" + actual:CambioEstadoBolsin.sosEnviado()
    public boolean sosEnviado() {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        return actual != null && actual.sosEnviado();
    }

    public Integer getNroPrecinto() {
        return numeroPrecinto;
    }

    // Bolsin -> ComisionMedica : getNombre()
    public String getCMOrigen() {
        return origen.getNombre();
    }

    // Loop: para todos los remitos del bolsín -> getDatosRemito()
    public List<String[]> obtenerInformacionRemito() {
        List<String[]> filas = new ArrayList<>();
        for (Remito r : remito) {
            filas.addAll(r.getDatosRemito());
        }
        return filas;
    }

    /**
     * Cambia el estado del bolsín y, en cascada, el de sus remitos y su documentación.
     *
     * Nota: el diagrama de diseño muestra actualizarEstado(estado, fechaHoraActual, responsable),
     * pero el bolsín también necesita el estado de remito para poder llamar a
     * actualizarEstadoRemito(estadoRemito, ...). Por eso se agrega el parámetro estadoRemito.
     */
    public void actualizarEstado(Estado estado, Estado estadoRemito, LocalDateTime fechaHoraActual,
            Empleado responsable) {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHoraActual);
        }
        cambioEstado.add(new CambioEstadoBolsin(fechaHoraActual, estado, responsable));

        actualizarEstadoRemito(estadoRemito, fechaHoraActual, responsable);
    }

    // Loop: para cada remito del bolsín -> setEstado()
    private void actualizarEstadoRemito(Estado estadoRemito, LocalDateTime fechaHoraActual, Empleado responsable) {
        for (Remito r : remito) {
            r.setEstado(estadoRemito, fechaHoraActual, responsable);
        }
    }

    /**
     * Flujos alternativos (A3, A4, A5): el bolsín cambia de estado igual que en el flujo básico,
     * pero cada remito recibe además la documentación marcada por el EB, la situación que le
     * corresponde y el estado de remito parcial.
     */
    public void actualizarEstadoConSituacion(Estado estado, Estado estadoRemito, Estado estadoRemitoParcial,
            List<Integer> documentacionMarcada, SituacionRecepcion situacion, LocalDateTime fechaHoraActual,
            Empleado responsable) {
        CambioEstadoBolsin actual = buscarCambioEstadoActual();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHoraActual);
        }
        cambioEstado.add(new CambioEstadoBolsin(fechaHoraActual, estado, responsable));

        // Loop: para cada remito del bolsín -> setEstadoConSituacion()
        for (Remito r : remito) {
            r.setEstadoConSituacion(estadoRemito, estadoRemitoParcial, documentacionMarcada, situacion,
                    fechaHoraActual, responsable);
        }
    }

    // Flujos alternativos: la documentación que marca el EB tiene que estar en el bolsín
    public boolean contieneDocumentacion(Integer numeroDocumentacion) {
        return getDocumentacion().stream().anyMatch(doc -> doc.getNumero().equals(numeroDocumentacion));
    }

    // En el diagrama vamos directo a "actual:CambioEstadoBolsin"; en código hay que buscarlo.
    private CambioEstadoBolsin buscarCambioEstadoActual() {
        return cambioEstado.stream()
                .filter(CambioEstadoBolsin::sosActual)
                .findFirst()
                .orElse(null);
    }

    public String getCMDestino() {
        return destino.getNombre();
    }

    // CU 29: toda la documentación del bolsín (recorriendo remitos y detalles)
    public List<Documentacion> getDocumentacion() {
        List<Documentacion> documentacion = new ArrayList<>();
        for (Remito r : remito) {
            for (DetalleRemito detalle : r.getDetalleRemito()) {
                documentacion.add(detalle.getDocumentacion());
            }
        }
        return documentacion;
    }

    public Integer getNumeroBolsin() {
        return numeroBolsin;
    }

    public List<Remito> getRemito() {
        return remito;
    }
}
