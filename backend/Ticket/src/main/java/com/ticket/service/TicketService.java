package com.ticket.service;

import com.ticket.dto.TicketDTO;
import com.ticket.dto.ComentarioDTO;
import com.ticket.dto.EstadoDTO;
import com.ticket.dto.TicketResponseDTO;
import java.time.LocalDateTime;
import com.ticket.entity.Ticket;
import com.ticket.entity.Comentario;
import com.ticket.entity.Usuario;
import com.ticket.entity.EstadoTicket;
import com.ticket.exception.NotFoundException;
import com.ticket.exception.BadRequestException;
import com.ticket.repository.TicketRepository;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
@Transactional
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

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
    public List<TicketResponseDTO> obtenerTicketsPorUsuario(String email) {
        List<Ticket> tickets = ticketRepository.findByCreadoPorEmail(email);
        return tickets.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private TicketResponseDTO convertToDTO(Ticket ticket) {
        TicketResponseDTO dto = new TicketResponseDTO();
        dto.setId(ticket.getId());
        dto.setTitulo(ticket.getTitulo());
        dto.setDescripcion(ticket.getDescripcion());
        dto.setEstado(ticket.getEstado().toString());
        dto.setFechaCreacion(ticket.getFechaCreacion());

        if (ticket.getComentarios() != null) {
            dto.setComentarios(ticket.getComentarios().stream()
                    .map(this::convertComentarioToDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setComentarios(new ArrayList<>());
        }

        return dto;
    }

    private ComentarioDTO convertComentarioToDTO(Comentario comentario) {
        ComentarioDTO dto = new ComentarioDTO();
        dto.setId(comentario.getId());
        dto.setContenido(comentario.getContenido());
        dto.setAutor(comentario.getAutor());
        dto.setFechaCreacion(comentario.getFechaCreacion());
        return dto;
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
    @Transactional
    public Ticket agregarComentario(Long ticketId, ComentarioDTO comentarioDTO, String autor) {
        logger.debug("Agregando comentario al ticket {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->{
                    logger.error("Ticket no encontrado: {}", ticketId);
                    return new NotFoundException("Ticket no encontrado");
                });


        //debug temporal
        logger.info("Estado del ticket: {}", ticket.getEstado());

        //validacion explicita del estado
        if (!EstadoTicket.ABIERTO.equals(ticket.getEstado())) {
            throw new BadRequestException("Estado inválido para comentar. Estado actual: " + ticket.getEstado());
        }

        //crear y guardar comentario
        Comentario comentario = new Comentario();
        comentario.setContenido(comentarioDTO.getContenido());
        comentario.setAutor(autor);
        comentario.setFechaCreacion(LocalDateTime.now());
        comentario.setTicket(ticket);

        ticket.getComentarios().add(comentario);
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