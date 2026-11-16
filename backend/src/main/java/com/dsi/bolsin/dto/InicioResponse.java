package com.dsi.bolsin.dto;

import java.util.List;

public record InicioResponse(String cmUsuario, List<BolsinResponse> bolsines) {
}
