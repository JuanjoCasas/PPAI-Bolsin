package com.dsi.bolsin.dto;

import java.util.List;

// Lo que la pantalla muestra al habilitarse: mostrarCMUsuario() + mostrarBolsines()
public record InicioResponse(String cmUsuario, List<BolsinResponse> bolsines) {
}
