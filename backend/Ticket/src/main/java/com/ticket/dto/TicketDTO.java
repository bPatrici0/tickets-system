package com.ticket.dto;

import com.ticket.entity.EstadoTicket;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TicketDTO {
    private String titulo;
    private String descripcion;
    private String estado;
}