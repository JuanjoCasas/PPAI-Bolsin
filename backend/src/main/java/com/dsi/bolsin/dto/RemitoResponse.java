package com.dsi.bolsin.dto;

import java.util.List;

public record RemitoResponse(Integer numero, List<DocumentacionResponse> documentaciones) {
}
