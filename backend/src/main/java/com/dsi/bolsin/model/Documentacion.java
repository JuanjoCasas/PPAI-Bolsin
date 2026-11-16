package com.dsi.bolsin.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.dsi.bolsin.model.estado.EstadoDocumentacion;
import com.dsi.bolsin.model.estado.EstadoDocumentacionConverter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Patrón State: Contexto.
 * Delega en su estado actual las operaciones de la máquina de estados.
 */
@Entity
@Table(name = "documentaciones")
public class Documentacion {

    @Id
    private Integer numero;

    @Column(nullable = false, length = 200)
    private String asunto;

    @Column
    private LocalDate fechaPase;

    // Documentacion -> TipoDocumento (1) : tipoDocumento
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_documento_nombre", nullable = false)
    private TipoDocumento tipoDocumento;

    // Documentacion -> ComisionMedica (1) : cmOrigen
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cm_origen_codigo", nullable = false)
    private ComisionMedica cmOrigen;

    // Documentacion -> EstadoDocumentacion (1) : estadoActual
    @Convert(converter = EstadoDocumentacionConverter.class)
    @Column(name = "estado_actual", nullable = false, length = 50)
    private EstadoDocumentacion estadoActual;

    // Documentacion -> CambioEstadoDocumentacion (1..*) : cambioEstado
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "documentacion_numero", nullable = false)
    private List<CambioEstadoDocumentacion> cambioEstado = new ArrayList<>();

    protected Documentacion() {
    }

    // ---- Datos que se muestran en la pantalla ----

    public String[] getDatosDocumentacion() {
        return new String[] { String.valueOf(numero), getAsuntoDocumentacion() };
    }

    public String getAsuntoDocumentacion() {
        return asunto;
    }

    // Documentacion -> TipoDocumento : getNombre()
    public String getTipoDocumentacion() {
        return tipoDocumento.getNombre();
    }

    // ---- Patrón State: delegación al estado actual ----

    public void aceptar(LocalDateTime fechaHoraActual, Empleado responsable) {
        estadoActual.aceptar(this, fechaHoraActual, responsable, cambioEstado);
    }

    public void setEstado(EstadoDocumentacion estado) {
        this.estadoActual = estado;
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambioEstado) {
        this.cambioEstado.add(cambioEstado);
    }

    // Demás transiciones de la ME: se delegan igual al estado actual
    public void incluirEnRemito() {
        estadoActual.incluirEnRemito();
    }

    public void cancelarRemito() {
        estadoActual.cancelarRemito();
    }

    public void agregarABolsin() {
        estadoActual.agregarABolsin();
    }

    public void modificarBolsin() {
        estadoActual.modificarBolsin();
    }

    public void quitarDeBolsin() {
        estadoActual.quitarDeBolsin();
    }

    public void retirarBolsin() {
        estadoActual.retirarBolsin();
    }

    public void docNoRecibida() {
        estadoActual.docNoRecibida();
    }

    public void recibirYRechazar() {
        estadoActual.recibirYRechazar();
    }

    public void redirigir() {
        estadoActual.redirigir();
    }

    public void reenviarDocumentacion() {
        estadoActual.reenviarDocumentacion();
    }

    public void eliminarDocumentacion() {
        estadoActual.eliminarDocumentacion();
    }

    // ---- Getters de soporte ----

    public Integer getNumero() {
        return numero;
    }

    // CU 29: a qué CM de origen hay que notificarle
    public ComisionMedica getCMOrigen() {
        return cmOrigen;
    }

    public EstadoDocumentacion getEstadoActual() {
        return estadoActual;
    }

    public List<CambioEstadoDocumentacion> getCambioEstado() {
        return cambioEstado;
    }
}
