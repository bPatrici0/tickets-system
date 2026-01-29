package com.ticket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ticketId;
    private String usuario;
    private String accion;
    private String valorAnterior;
    private String valorNuevo;
    private String detalles;
    private LocalDateTime fecha = LocalDateTime.now();

}
