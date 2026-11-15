package com.dsi.bolsin.dto;

import java.util.List;

// mostrarDetalleBolsin() + mostrarOpcionesRecepcionBolsin()
public record DetalleBolsinResponse(
        Integer numeroPrecinto,
        String cmOrigen,
        List<RemitoResponse> remitos,
        List<String> opcionesRecepcion) {
}
