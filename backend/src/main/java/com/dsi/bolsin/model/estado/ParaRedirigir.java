package com.dsi.bolsin.model.estado;

import java.time.LocalDateTime;
import java.util.List;

import com.dsi.bolsin.model.CambioEstadoDocumentacion;
import com.dsi.bolsin.model.Documentacion;
import com.dsi.bolsin.model.Empleado;

/**
 * Desde ParaRedirigir, aceptar() también es válido (CU 31: ParaRedirigir -> RecibidaYAceptada).
 */
public class ParaRedirigir extends EstadoDocumentacion {

    public static final String NOMBRE = "ParaRedirigir";

    public ParaRedirigir() {
        super(NOMBRE, "La documentación llegó a la CMC y debe redirigirse");
    }

    @Override
    public void aceptar(Documentacion documentacion, LocalDateTime fechaHoraActual, Empleado responsable,
            List<CambioEstadoDocumentacion> historiales) {
        for (CambioEstadoDocumentacion cambioEstado : historiales) {
            if (cambioEstado.sosActual()) {
                cambioEstado.setFechaHoraFin(fechaHoraActual);
            }
        }
        EstadoDocumentacion nuevoEstado = new RecibidaYAceptada();
        documentacion.setEstado(nuevoEstado);
        documentacion.agregarCambioEstado(new CambioEstadoDocumentacion(fechaHoraActual, nuevoEstado, responsable));
    }

    @Override
    public void recibirYRechazar() {
        throw fueraDeAlcance("recibirYRechazar");
    }
}
