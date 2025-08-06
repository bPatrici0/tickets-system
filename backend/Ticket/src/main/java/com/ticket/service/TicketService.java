package com.ticket.service;

import com.ticket.dto.TicketDTO;
import com.ticket.dto.ComentarioDTO;
import java.time.LocalDateTime;
import com.ticket.entity.Ticket;
import com.ticket.entity.Comentario;
import com.ticket.entity.Usuario;
import com.ticket.exception.NotFoundException;
import com.ticket.exception.BadRequestException;
import com.ticket.repository.TicketRepository;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Ticket crearTicket(TicketDTO ticketDTO) {
        if (ticketDTO.getTitulo() == null || ticketDTO.getTitulo().isEmpty()) {
            throw new BadRequestException("El titulo del ticket es requerido");
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Ticket ticket = new Ticket();
        ticket.setTitulo(ticketDTO.getTitulo());
        ticket.setDescripcion(ticketDTO.getDescripcion());

        try {
            ticket.setEstado(ticketDTO.getEstado() != null ?
                    Ticket.EstadoTicket.valueOf(ticketDTO.getEstado()) :
                    Ticket.EstadoTicket.ABIERTO);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado de ticket inválido");
        }
        ticket.setCreadoPor(usuario);

        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> obtenerTodosLosTickets() {
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ticket obtenerTicketPorId(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Ticket> obtenerTicketsPorUsuario(String email) {
        if (!usuarioRepository.existsByEmail(email)){
            throw new NotFoundException("Usuario no encontrado con email: " + email);
        }
        return ticketRepository.findByCreadoPorEmail(email);
    }

    public Ticket actualizarTicket(Long id, TicketDTO ticketDTO) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        if (ticketDTO.getTitulo() != null){
            ticket.setTitulo(ticketDTO.getTitulo());
        }
        if (ticketDTO.getDescripcion() != null) {
            ticket.setDescripcion(ticketDTO.getDescripcion());
        }
        if (ticketDTO.getEstado() != null) {
            try {
                ticket.setEstado(Ticket.EstadoTicket.valueOf(ticketDTO.getEstado()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Estado de ticket inválido!...");
            }
        }

        return ticketRepository.save(ticket);
    }

    public void eliminarTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new NotFoundException("Ticket no encontrado con id: " + id);
        }
        ticketRepository.deleteById(id);
    }

    public Ticket agregarComentario(Long ticketId, ComentarioDTO comentarioDTO) {
        Ticket ticket = obtenerTicketPorId(ticketId);

        Comentario comentario = new Comentario();
        comentario.setContenido(comentarioDTO.getContenido());
        comentario.setAutor(comentarioDTO.getAutor());
        comentario.setFechaCreacion(LocalDateTime.now());
        comentario.setTicket(ticket);

        ticket.agregarComentario(comentario);
        return ticketRepository.save(ticket);
    }

    public Ticket cambiarEstado(Long ticketId, String estado){
        Ticket ticket = obtenerTicketPorId(ticketId);
        try {
            ticket.setEstado(Ticket.EstadoTicket.valueOf(estado));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado de ticket inválido!...");
        }
        return ticketRepository.save(ticket);
    }
}