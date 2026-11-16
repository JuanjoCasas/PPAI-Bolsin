package com.dsi.bolsin.model.estado;

import java.time.LocalDateTime;
import java.util.List;

import com.dsi.bolsin.model.CambioEstadoDocumentacion;
import com.dsi.bolsin.model.Documentacion;
import com.dsi.bolsin.model.Empleado;

/**
 * Estado concreto en tiempo de ejecución durante el CU 28.
 * Implementa aceptar(): EnBolsinEnviado -> RecibidaYAceptada.
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    public static final String NOMBRE = "EnBolsinEnviado";

    // new(): EnBolsinEnviado
    public EnBolsinEnviado() {
        super(NOMBRE, "La documentación viaja en un bolsín enviado a la CM destino");
    }

    @Override
    public void aceptar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable,
            List<CambioEstadoDocumentacion> historiales) {
        // 1. Cierra el cambio de estado actual
        cambiarEstado(fechaHoraActual, historiales);

        // 2. Crea el nuevo estado
        EstadoDocumentacion nuevoEstado = crearEstado();

        // 3. Crea el nuevo cambio de estado
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHoraActual, nuevoEstado, responsable);

        // 4. Transición: le indica a la documentación su nuevo estado y su nuevo cambio de estado
        documentacion.setEstado(nuevoEstado);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
    }

    // Loop "* sosActual()" sobre los historiales + actual:CambioEstadoDocumentacion.setFechaHoraFin()
    private void cambiarEstado(LocalDateTime fechaHoraActual, List<CambioEstadoDocumentacion> historiales) {
        for (CambioEstadoDocumentacion cambioEstado : historiales) {
            if (cambioEstado.sosActual()) {
                cambioEstado.setFechaHoraFin(fechaHoraActual);
            }
        }
    }

    private RecibidaYAceptada crearEstado() {
        return new RecibidaYAceptada();
    }

    private CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fechaHoraActual, EstadoDocumentacion estado,
            Empleado responsable) {
        return new CambioEstadoDocumentacion(fechaHoraActual, estado, responsable);
    }

    // ---- Transiciones válidas desde EnBolsinEnviado que pertenecen a otros flujos / CU ----

    @Override
    public void docNoRecibida() {
        throw fueraDeAlcance("docNoRecibida");
    }

    @Override
    public void recibirYRechazar() {
        throw fueraDeAlcance("recibirYRechazar");
    }

    @Override
    public void redirigir() {
        throw fueraDeAlcance("redirigir");
    }
}
