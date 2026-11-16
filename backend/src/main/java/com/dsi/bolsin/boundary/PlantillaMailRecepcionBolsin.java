package com.dsi.bolsin.boundary;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parte visual del mail del CU 29 (no figura en los diagramas).
 *
 * Arma el cuerpo HTML con los mismos colores y tipografía que el front (front/src/theme.js).
 * Los estilos van inline y el diseño con tablas porque los clientes de correo (Gmail, Outlook)
 * ignoran las hojas de estilo y buena parte del CSS moderno.
 */
public final class PlantillaMailRecepcionBolsin {

    // Paleta de front/src/theme.js
    private static final String PRIMARY = "#1a9c8f";
    private static final String PRIMARY_DARK = "#00796b";
    private static final String SECONDARY = "#d32f2f";
    private static final String BACKGROUND = "#eef8f6";
    private static final String TABLE_HEAD = "#e0f4f1";
    private static final String TEXT = "#1f2933";
    private static final String TEXT_SECONDARY = "#5f6b6d";
    private static final String DIVIDER = "#dfe7e5";
    private static final String FONT = "Roboto, Helvetica, Arial, sans-serif";

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Una fila de la tabla: datos de la documentación y el estado en que quedó. */
    public record Fila(String numero, String asunto, String tipo, String estado) {
    }

    /** Cómo se muestra cada estado: etiqueta, colores del chip y qué tiene que hacer la CM origen. */
    private record Presentacion(String etiqueta, String color, String fondo, String accion) {
    }

    // Colores de success / warning / error / info del theme, con fondo suave para el chip
    private static final Map<String, Presentacion> PRESENTACION = Map.of(
            "RecibidaYAceptada", new Presentacion("Recibida y aceptada", "#2e9d6b", "#e3f4ec", null),
            "NoRecibida", new Presentacion("No recibida", "#b26a00", "#fff3e0",
                    "La documentación no recibida puede volver a registrarse para incluirla en un nuevo remito, "
                            + "o darse de baja si ya no es relevante su envío."),
            "RecibidaYRechazada", new Presentacion("Recibida y rechazada", SECONDARY, "#fdecea",
                    "La documentación rechazada no corresponde a la CM destino: se devuelve al origen para "
                            + "asociarla a un nuevo remito dirigido a la CM correcta."),
            "ParaRedirigir", new Presentacion("Para redirigir", PRIMARY_DARK, TABLE_HEAD,
                    "La documentación para redirigir se envía internamente al área que corresponde "
                            + "dentro de la sede central."));

    private PlantillaMailRecepcionBolsin() {
    }

    public static String armarHtml(String cmOrigen, String cmDestino, Integer nroPrecinto,
            LocalDateTime fechaRecepcion, List<Fila> filas) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>Recepción del bolsín</title></head>")
                .append("<body style=\"margin:0;padding:0;background:").append(BACKGROUND).append(";\">")
                .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"")
                .append("background:").append(BACKGROUND).append(";padding:32px 16px;font-family:").append(FONT)
                .append(";color:").append(TEXT).append(";\"><tr><td align=\"center\">")
                .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"")
                .append("max-width:640px;background:#ffffff;border-radius:12px;overflow:hidden;")
                .append("box-shadow:0 2px 8px rgba(0,0,0,0.1);\">");

        // Encabezado: igual que el AppBar del front (teal con borde inferior rojo)
        html.append("<tr><td style=\"background:").append(PRIMARY).append(";border-bottom:3px solid ")
                .append(SECONDARY).append(";padding:20px 32px;\">")
                .append("<div style=\"font-size:13px;color:#e0f4f1;letter-spacing:0.5px;\">")
                .append("Gestión de bolsines</div>")
                .append("<div style=\"font-size:20px;font-weight:500;color:#ffffff;margin-top:4px;\">")
                .append("Recepción de bolsín registrada</div></td></tr>");

        // Saludo y datos del bolsín
        html.append("<tr><td style=\"padding:28px 32px 8px 32px;\">")
                .append("<p style=\"margin:0 0 12px 0;font-size:15px;\">Sres. <strong>").append(esc(cmOrigen))
                .append("</strong>:</p>")
                .append("<p style=\"margin:0;font-size:15px;line-height:1.5;color:").append(TEXT_SECONDARY)
                .append(";\">Les informamos que se registró la recepción del bolsín en <strong style=\"color:")
                .append(TEXT).append(";\">").append(esc(cmDestino))
                .append("</strong>. A continuación se detalla la situación de la documentación que enviaron.</p>")
                .append("</td></tr>");

        html.append("<tr><td style=\"padding:16px 32px;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"")
                .append("border-left:4px solid ").append(SECONDARY).append(";background:").append(BACKGROUND)
                .append(";border-radius:8px;\"><tr>")
                .append(dato("Precinto", "N° " + nroPrecinto))
                .append(dato("Destino", cmDestino))
                .append(dato("Fecha de recepción", fechaRecepcion.format(FORMATO_FECHA)))
                .append("</tr></table></td></tr>");

        // Resumen por estado
        Map<String, Integer> cantidades = new LinkedHashMap<>();
        for (Fila f : filas) {
            cantidades.merge(f.estado(), 1, Integer::sum);
        }
        html.append("<tr><td style=\"padding:8px 32px 0 32px;font-size:14px;color:").append(TEXT_SECONDARY)
                .append(";\">").append(filas.size()).append(filas.size() == 1 ? " documentación: " : " documentaciones: ");
        cantidades.forEach((estado, cantidad) -> html.append(chip(etiqueta(estado) + " (" + cantidad + ")",
                estado)).append(" "));
        html.append("</td></tr>");

        // Tabla de documentación: mismo estilo que las grillas del front
        html.append("<tr><td style=\"padding:16px 32px;\">")
                .append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border:1px solid ")
                .append(DIVIDER).append(";border-radius:8px;border-collapse:separate;overflow:hidden;")
                .append("font-size:14px;\"><tr style=\"background:").append(TABLE_HEAD).append(";\">")
                .append(th("N°")).append(th("Asunto")).append(th("Tipo")).append(th("Situación"))
                .append("</tr>");
        for (Fila f : filas) {
            html.append("<tr>")
                    .append(td(esc(f.numero()), "font-weight:500;white-space:nowrap;"))
                    .append(td(esc(f.asunto()), ""))
                    .append(td(esc(f.tipo()), "color:" + TEXT_SECONDARY + ";"))
                    .append(td(chip(etiqueta(f.estado()), f.estado()), "white-space:nowrap;"))
                    .append("</tr>");
        }
        html.append("</table></td></tr>");

        // Qué tiene que hacer la CM origen (regla de negocio "Recepción de un Bolsín en una CM Destino")
        List<String> acciones = cantidades.keySet().stream()
                .map(PRESENTACION::get)
                .filter(p -> p != null && p.accion() != null)
                .map(Presentacion::accion)
                .toList();
        if (!acciones.isEmpty()) {
            html.append("<tr><td style=\"padding:0 32px 16px 32px;\">")
                    .append("<div style=\"background:#fff8e1;border-left:4px solid #ff9800;border-radius:8px;")
                    .append("padding:14px 18px;font-size:14px;line-height:1.5;\">")
                    .append("<div style=\"font-weight:500;margin-bottom:6px;\">Acciones pendientes</div>");
            for (String accion : acciones) {
                html.append("<div style=\"color:").append(TEXT_SECONDARY).append(";margin-top:4px;\">• ")
                        .append(esc(accion)).append("</div>");
            }
            html.append("</div></td></tr>");
        }

        // Pie
        html.append("<tr><td style=\"padding:20px 32px 28px 32px;border-top:1px solid ").append(DIVIDER)
                .append(";font-size:12px;color:").append(TEXT_SECONDARY).append(";line-height:1.5;\">")
                .append("Este mensaje fue generado automáticamente por el sistema de gestión de bolsines ")
                .append("al registrarse la recepción (CU 29 Notificar recepción de bolsín). No responda a este correo.")
                .append("</td></tr>");

        html.append("</table></td></tr></table></body></html>");
        return html.toString();
    }

    private static String dato(String etiqueta, String valor) {
        return "<td style=\"padding:12px 16px;vertical-align:top;\">"
                + "<div style=\"font-size:12px;color:" + TEXT_SECONDARY + ";\">" + esc(etiqueta) + "</div>"
                + "<div style=\"font-size:15px;font-weight:500;margin-top:2px;\">" + esc(valor) + "</div></td>";
    }

    private static String th(String texto) {
        return "<th align=\"left\" style=\"padding:10px 12px;font-weight:500;color:" + PRIMARY_DARK + ";\">"
                + texto + "</th>";
    }

    private static String td(String contenido, String estilo) {
        return "<td style=\"padding:10px 12px;border-top:1px solid " + DIVIDER + ";vertical-align:middle;"
                + estilo + "\">" + contenido + "</td>";
    }

    private static String chip(String texto, String estado) {
        Presentacion p = PRESENTACION.get(estado);
        String color = p != null ? p.color() : TEXT_SECONDARY;
        String fondo = p != null ? p.fondo() : "#eceff1";
        return "<span style=\"display:inline-block;padding:3px 10px;border-radius:12px;font-size:12px;"
                + "font-weight:500;color:" + color + ";background:" + fondo + ";\">" + esc(texto) + "</span>";
    }

    private static String etiqueta(String estado) {
        Presentacion p = PRESENTACION.get(estado);
        return p != null ? p.etiqueta() : estado;
    }

    private static String esc(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
