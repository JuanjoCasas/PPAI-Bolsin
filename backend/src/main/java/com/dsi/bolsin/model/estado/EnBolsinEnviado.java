package com.dsi.bolsin.model.estado;

import java.time.LocalDateTime;
import java.util.List;

import com.dsi.bolsin.model.CambioEstadoDocumentacion;
import com.dsi.bolsin.model.Documentacion;
import com.dsi.bolsin.model.Empleado;

/**
 * Estado concreto en tiempo de ejecución durante el CU 28.
 * Implementa las cuatro transiciones que salen de este estado en la recepción del bolsín:
 * - aceptar():          EnBolsinEnviado -> RecibidaYAceptada  (flujo básico)
 * - docNoRecibida():    EnBolsinEnviado -> NoRecibida          (A3)
 * - recibirYRechazar(): EnBolsinEnviado -> RecibidaYRechazada  (A4)
 * - redirigir():        EnBolsinEnviado -> ParaRedirigir       (A5)
 */
public class EnBolsinEnviado extends EstadoDocumentacion {

    public static final String NOMBRE = "EnBolsinEnviado";

    // new(): EnBolsinEnviado
    public EnBolsinEnviado() {
        super(NOMBRE, "La documentación viaja en un bolsín enviado a la CM destino");
    }

    // Flujo básico: sigue paso a paso el diagrama de secuencia de diseño
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

    // A3: no se recibe toda la documentación. Mismos pasos que aceptar(), pero crea NoRecibida.
    @Override
    public void docNoRecibida(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstado(fechaHoraActual, historiales);
        EstadoDocumentacion nuevoEstado = crearEstadoNoRecibida();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHoraActual, nuevoEstado, responsable);
        documentacion.setEstado(nuevoEstado);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
    }

    // A4: la documentación no corresponde a la CM destino. Crea RecibidaYRechazada.
    @Override
    public void recibirYRechazar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstado(fechaHoraActual, historiales);
        EstadoDocumentacion nuevoEstado = crearEstadoRecibidaYRechazada();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHoraActual, nuevoEstado, responsable);
        documentacion.setEstado(nuevoEstado);
        documentacion.agregarCambioEstado(nuevoCambioEstado);
    }

    // A5: la documentación se debe redirigir a otra área de la CMC. Crea ParaRedirigir.
    @Override
    public void redirigir(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstado(fechaHoraActual, historiales);
        EstadoDocumentacion nuevoEstado = crearEstadoParaRedirigir();
        CambioEstadoDocumentacion nuevoCambioEstado = crearCambioEstado(fechaHoraActual, nuevoEstado, responsable);
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

    private NoRecibida crearEstadoNoRecibida() {
        return new NoRecibida();
    }

    private RecibidaYRechazada crearEstadoRecibidaYRechazada() {
        return new RecibidaYRechazada();
    }

    private ParaRedirigir crearEstadoParaRedirigir() {
        return new ParaRedirigir();
    }

    private CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fechaHoraActual, EstadoDocumentacion estado,
            Empleado responsable) {
        return new CambioEstadoDocumentacion(fechaHoraActual, estado, responsable);
    }
}
