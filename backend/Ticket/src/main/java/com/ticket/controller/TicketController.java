package com.ticket.controller;

import com.ticket.dto.TicketDTO;
import com.ticket.dto.ComentarioDTO;
import com.ticket.dto.TicketResponseDTO;
import com.ticket.dto.EstadoDTO;
import com.ticket.entity.Ticket;
import com.ticket.entity.EstadoTicket;
import com.ticket.service.TicketService;
import com.ticket.exception.NotFoundException;
import com.ticket.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tickets")
@Slf4j
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
    public ResponseEntity<List<TicketResponseDTO>> obtenerTicketsPorUsuario(@PathVariable String email) {
        List<TicketResponseDTO> tickets = ticketService.obtenerTicketsPorUsuario(email);
        return ResponseEntity.ok(tickets);
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
    public ResponseEntity<?> agregarComentario(@PathVariable Long id, @RequestBody ComentarioDTO comentarioDTO, Authentication authentication) {
        log.info("intentando agregar comentario al ticket {} por usuario {}", id, authentication.getName());
        try {
            log.info("Intento de agregar comentario. Tciket ID: {}, Usuario: {}, Contenido: {}",
                id, authentication.getName(), comentarioDTO.getContenido());
            if (comentarioDTO.getContenido() == null || comentarioDTO.getContenido().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("El contenido del comentario no puede estar vacio");
            }

            Ticket ticket = ticketService.obtenerTicketPorId(id);
            log.info("Estado REAL del ticket {}: {} - Tipo: {}",
                id, ticket.getEstado(), ticket.getEstado().getClass());

            if (!ticket.getEstado().equals(EstadoTicket.ABIERTO)) {
                log.error("Intento de comentar ticket {} con estado {}", id, ticket.getEstado());
                return ResponseEntity.badRequest()
                        .body("Solo se pueden agregar comentarios en tickets ABIERTOS. Estado actual: " + ticket.getEstado());
            }

            //agregar comentario
            Ticket ticketActualizado = ticketService.agregarComentario(id, comentarioDTO, authentication.getName());
            return ResponseEntity.ok(ticketActualizado);
        }catch (Exception e) {
            log.error("Error al agregar comentario", e);
            return ResponseEntity.internalServerError().body("error interno al agregar comentario" + e.getMessage());
        }
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioDTO>> obtenerComentarios(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.obtenerComtariosPorTicket(id));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Ticket> cambiarEstado(@PathVariable Long id, @RequestBody EstadoDTO estadoDTO) {
        return ResponseEntity.ok(ticketService.cambiarEstado(id, estadoDTO.getEstado()));
    }
}