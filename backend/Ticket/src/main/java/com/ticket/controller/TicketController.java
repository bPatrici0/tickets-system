package com.ticket.controller;

import com.ticket.dto.TicketDTO;
import com.ticket.dto.ComentarioDTO;
import com.ticket.dto.EstadoDTO;
import com.ticket.entity.Ticket;
import com.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    @PreAuthorize("isAuthenticated()") //solo usuarios logueados pueden crear tickets
    public ResponseEntity<Ticket> crearTicket(@RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.crearTicket(ticketDTO));
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> obtenerTodosLosTickets() {
        return ResponseEntity.ok(ticketService.obtenerTodosLosTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> obtenerTicketPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.obtenerTicketPorId(id));
    }

    @GetMapping("/usuario/{email}")
    public ResponseEntity<List<Ticket>> obtenerTicketsPorUsuario(@PathVariable String email) {
        return ResponseEntity.ok(ticketService.obtenerTicketsPorUsuario(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> actualizarTicket(@PathVariable Long id, @RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.actualizarTicket(id, ticketDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        ticketService.eliminarTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comentarios")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Ticket> agregarComentario(@PathVariable Long id, @RequestBody ComentarioDTO comentarioDTO) {
        return ResponseEntity.ok(ticketService.agregarComentario(id, comentarioDTO));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Ticket> cambiarEstado(@PathVariable Long id, @RequestBody EstadoDTO estadoDTO) {
        return ResponseEntity.ok(ticketService.cambiarEstado(id, estadoDTO.getEstado()));
    }
}