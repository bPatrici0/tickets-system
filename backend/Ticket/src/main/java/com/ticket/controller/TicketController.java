package com.ticket.controller;

import com.ticket.dto.TicketDTO;
import com.ticket.dto.ComentarioDTO;
import com.ticket.dto.TicketResponseDTO;
import com.ticket.dto.EstadoDTO;
import com.ticket.entity.Ticket;
import com.ticket.entity.EstadoTicket;
import com.ticket.entity.Comentario;
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
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/tickets")
@Slf4j
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @PostMapping("/{id}/comentarios")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> agregarComentario(@PathVariable Long id, @RequestBody ComentarioDTO comentarioDTO,
            Authentication authentication) {
        log.info("intentando agregar comentario al ticket {} por usuario {}", id, authentication.getName());

        try {
            if (comentarioDTO.getContenido() == null || comentarioDTO.getContenido().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El contenido no puede estar vacio");
            }

            ComentarioDTO comentarioCreado = ticketService.agregarComentario(id, comentarioDTO,
                    authentication.getName());

            return ResponseEntity.ok(comentarioCreado);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error al agregar comentario", e);
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
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

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioDTO>> obtenerComentarios(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.obtenerComentariosPorTicket(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()") // solo usuarios logueados pueden crear tickets
    public ResponseEntity<Ticket> crearTicket(@RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.crearTicket(ticketDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ticket> actualizarTicket(@PathVariable Long id, @RequestBody TicketDTO ticketDTO) {
        return ResponseEntity.ok(ticketService.actualizarTicket(id, ticketDTO));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @RequestBody EstadoDTO estadoDTO) {
        try {
            if (estadoDTO.getEstado() == null || estadoDTO.getEstado().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El campo 'estado' es requerido");
            }

            Ticket ticketActualizado = ticketService.cambiarEstadoTicket(id, estadoDTO.getEstado());
            return ResponseEntity.ok(ticketActualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        ticketService.eliminarTicket(id);
        return ResponseEntity.noContent().build();
    }
}