package com.ticket.dto;

public class PerformanceDTO {
    private String usuario;
    private Long cantidadResoluciones;

    public PerformanceDTO(String usuario, Long cantidadResoluciones) {
        this.usuario = usuario;
        this.cantidadResoluciones = cantidadResoluciones;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Long getCantidadResoluciones() {
        return cantidadResoluciones;
    }

    public void setCantidadResoluciones(Long cantidadResoluciones) {
        this.cantidadResoluciones = cantidadResoluciones;
    }
}
