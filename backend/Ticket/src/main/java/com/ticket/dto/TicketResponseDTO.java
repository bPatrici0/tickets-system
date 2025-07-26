package com.ticket.dto;

import com.ticket.entity.EstadoTicket;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketResponseDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private EstadoTicket estado;
    private String creadoPor; // Email del usuario
    private LocalDateTime fechaCreacion;
}