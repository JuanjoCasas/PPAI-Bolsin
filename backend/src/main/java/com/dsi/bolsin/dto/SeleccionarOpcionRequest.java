package com.dsi.bolsin.dto;

import java.util.List;

// documentaciones: solo en los flujos alternativos A3, A4 y A5 (números de la documentación marcada)
public record SeleccionarOpcionRequest(String opcion, List<Integer> documentaciones) {
}
