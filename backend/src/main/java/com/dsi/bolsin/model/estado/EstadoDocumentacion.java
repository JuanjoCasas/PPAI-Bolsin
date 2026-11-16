package com.dsi.bolsin.model.estado;

import java.time.LocalDateTime;
import java.util.List;

import com.dsi.bolsin.model.CambioEstadoDocumentacion;
import com.dsi.bolsin.model.Documentacion;
import com.dsi.bolsin.model.Empleado;

/**
 * Patrón State: clase abstracta Estado.
 *
 * Declara todos los métodos de la máquina de estados de Documentación.
 * La implementación por defecto RECHAZA la transición; cada estado concreto
 * sobrescribe solo las transiciones válidas desde él.
 *
 * No es una @Entity: se persiste como texto (su nombre) mediante
 * {@link EstadoDocumentacionConverter}.
 */
public abstract class EstadoDocumentacion {

    private final String nombre;
    private final String descripcion;

    protected EstadoDocumentacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // ---- Transiciones de la máquina de estados (por defecto se rechazan) ----

    public void aceptar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable,
            List<CambioEstadoDocumentacion> historiales) {
        throw transicionInvalida("aceptar");
    }

    public void incluirEnRemito() {
        throw transicionInvalida("incluirEnRemito");
    }

    public void cancelarRemito() {
        throw transicionInvalida("cancelarRemito");
    }

    public void agregarABolsin() {
        throw transicionInvalida("agregarABolsin");
    }

    public void modificarBolsin() {
        throw transicionInvalida("modificarBolsin");
    }

    public void quitarDeBolsin() {
        throw transicionInvalida("quitarDeBolsin");
    }

    public void retirarBolsin() {
        throw transicionInvalida("retirarBolsin");
    }

    public void docNoRecibida() {
        throw transicionInvalida("docNoRecibida");
    }

    public void recibirYRechazar() {
        throw transicionInvalida("recibirYRechazar");
    }

    public void redirigir() {
        throw transicionInvalida("redirigir");
    }

    public void reenviarDocumentacion() {
        throw transicionInvalida("reenviarDocumentacion");
    }

    public void eliminarDocumentacion() {
        throw transicionInvalida("eliminarDocumentacion");
    }

    // ---- Auxiliares (no forman parte del diseño) ----

    private TransicionInvalidaException transicionInvalida(String transicion) {
        return new TransicionInvalidaException(
                "La transición " + transicion + "() no es válida desde el estado " + nombre);
    }

    /**
     * Las transiciones que salen de un estado según la ME pero que pertenecen a otros CU
     * (por ejemplo, el CU 31) se sobrescriben con esta llamada: son válidas, pero quedan
     * fuera del alcance de la implementación del CU 28.
     */
    protected UnsupportedOperationException fueraDeAlcance(String transicion) {
        return new UnsupportedOperationException(
                "La transición " + transicion + "() desde " + nombre
                        + " es válida, pero no se implementa en el CU 28");
    }

    // Dos objetos estado son "iguales" si son de la misma clase (no tienen otros datos).
    // Lo necesita Hibernate para detectar cambios en los atributos convertidos.
    @Override
    public boolean equals(Object o) {
        return o != null && o.getClass() == getClass();
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return nombre;
    }
}
