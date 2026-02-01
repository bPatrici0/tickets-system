package com.ticket.dto;

public class PerformanceDTO {
    private String usuario;
    private Long cantidadResoluciones;

    public PerformanceDTO(String usuario, Long cantidadResoluciones) {
        this.usuario = usuario;
        this.cantidadResoluciones = cantidadResoluciones;
    }
}
