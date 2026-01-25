package com.ticket.dto;

import com.ticket.entity.EstadoTicket;
import com.ticket.entity.PrioridadTicket;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketDTO {
    private String titulo;
    private String descripcion;
    private String estado;
    private String categoria;
    private String prioridad;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}