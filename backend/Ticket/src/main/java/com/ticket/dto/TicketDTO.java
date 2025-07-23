package com.ticket.dto;

import com.ticket.entity.Ticket.EstadoTicket;
import lombok.Data;

@Data
public class TicketDTO {
    private String titulo;
    private String descripcion;
    private EstadoTicket estado;
    private String emailUsuario; //para asociar el ticket al usuario
}