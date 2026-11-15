package com.dsi.bolsin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Estado de Bolsín y de Remito. Se mantiene sin cambios respecto del análisis.
 * Los estados de Documentación NO usan esta clase: se resuelven con el patrón State
 * (ver {@link com.dsi.bolsin.model.estado.EstadoDocumentacion}).
 */
@Entity
@Table(name = "estados")
public class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";

    @Id
    private Integer id;

    @Column(nullable = false, length = 50)
    private String ambito;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    protected Estado() {
    }

    public Estado(Integer id, String ambito, String nombre, String descripcion) {
        this.id = id;
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equals(ambito);
    }

    public boolean esAmbitoRemito() {
        return AMBITO_REMITO.equals(ambito);
    }

    public boolean esEnviado() {
        return "Enviado".equals(nombre);
    }

    public boolean esRecibidoENCMDestino() {
        return "Recibido en CM destino".equals(nombre);
    }

    public boolean esRecibidoYAceptado() {
        return "Recibido y Aceptado".equals(nombre);
    }

    public String getAmbito() {
        return ambito;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
