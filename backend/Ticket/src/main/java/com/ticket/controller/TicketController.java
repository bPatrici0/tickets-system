package com.ticket.controller;

import com.ticket.dto.TicketDTO;
import com.ticket.entity.Ticket;
import com.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    @Autowired
    private TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<Ticket>> obtenerTodosLosTickets() {
        return ResponseEntity.ok(ticketService.obtenerTodosLosTickets());
    }

    @PostMapping
    public ResponseEntity<Ticket> crearTciket(@RequestBody TicketDTO ticketDTO) {
        Ticket ticket = ticketService.crearTicket(ticketDTO);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/usuario/{email}")
    public ResponseEntity<List<Ticket>> obtenerTicketsPorUsuario(@PathVariable String email) {
        return ResponseEntity.ok(ticketService.obtenerTicketsPorUsuario(email));
    }
}