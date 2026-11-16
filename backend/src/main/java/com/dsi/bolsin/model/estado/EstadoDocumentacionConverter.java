package com.dsi.bolsin.model.estado;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persistencia (no es parte del diseño): guarda el estado de la documentación como
 * su nombre en una columna de texto, y al leer la BD vuelve a crear el objeto estado
 * concreto que corresponde.
 *
 * Este switch es el ÚNICO lugar donde se pregunta por el nombre del estado y solo sirve
 * para reconstruir el objeto. El comportamiento (las transiciones) sigue resolviéndose
 * por polimorfismo.
 */
@Converter
public class EstadoDocumentacionConverter implements AttributeConverter<EstadoDocumentacion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoDocumentacion estado) {
        return estado == null ? null : estado.getNombre();
    }

    @Override
    public EstadoDocumentacion convertToEntityAttribute(String nombre) {
        if (nombre == null) {
            return null;
        }
        return switch (nombre) {
            case Registrada.NOMBRE -> new Registrada();
            case EnRemito.NOMBRE -> new EnRemito();
            case EnBolsinSaliente.NOMBRE -> new EnBolsinSaliente();
            case EnBolsinEnviado.NOMBRE -> new EnBolsinEnviado();
            case NoRecibida.NOMBRE -> new NoRecibida();
            case RecibidaYRechazada.NOMBRE -> new RecibidaYRechazada();
            case ParaRedirigir.NOMBRE -> new ParaRedirigir();
            case RecibidaYAceptada.NOMBRE -> new RecibidaYAceptada();
            case DeBaja.NOMBRE -> new DeBaja();
            default -> throw new IllegalArgumentException("Estado de documentación desconocido: " + nombre);
        };
    }
}
