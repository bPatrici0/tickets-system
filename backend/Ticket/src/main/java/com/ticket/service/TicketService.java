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
import com.ticket.repository.ComentarioRepository;
import java.util.Date;
import java.util.Arrays;
import java.util.Optional;

@Service
@Transactional
public class TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private NotificationService notificationService;

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
            ticket.setEstado(
                    ticketDTO.getEstado() != null ? EstadoTicket.valueOf(ticketDTO.getEstado()) : EstadoTicket.ABIERTO);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado de ticket inválido");
        }
        ticket.setCreadoPor(usuario);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Notificar a los administradores sobre el nuevo ticket
        notificationService
                .notifyAdmins("NUEVO_TICKET: " + savedTicket.getTitulo() + " (ID: " + savedTicket.getId() + ")");

        return savedTicket;

    }

    @Transactional(readOnly = true)
    public List<Ticket> obtenerTodosLosTickets() {
        logger.debug("TicketService.obtenerTodosLosTickets() llamado");
        List<Ticket> tickets = ticketRepository.findAll();

        return tickets;
    }

    @Transactional(readOnly = true)
    public Ticket obtenerTicketPorId(Long id) {
        logger.debug("TicketService.obtenerTicketPorId() llamado - ID: {}", id);
        Optional<Ticket> ticketOpt = ticketRepository.findById(id);

        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            logger.debug("Ticket encontrado: {}", ticket.getTitulo());
            return ticket;
        } else {
            logger.warn("Ticket no encontrado - ID: {}", id);
            throw new NotFoundException("Ticket no encontrado con ID: " + id);
        }
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

        ComentarioDTO.AutorInfo autorInfo = new ComentarioDTO.AutorInfo();
        String emailAutor = comentario.getAutor();

        autorInfo.setEmail(emailAutor);
        autorInfo.setNombre("Usuario");
        autorInfo.setRol("ROLE_USER");

        if (emailAutor != null) {
            usuarioRepository.findByEmail(emailAutor).ifPresent(usuario -> {
                autorInfo.setNombre(usuario.getNombre() != null ? usuario.getNombre() : "Usuario");
                autorInfo.setRol(usuario.getRol());
            });
        }

        dto.setAutor(autorInfo);
        dto.setFechaCreacion(comentario.getFechaCreacion());
        return dto;
    }

    public Ticket actualizarTicket(Long id, TicketDTO ticketDTO) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        if (ticketDTO.getTitulo() != null) {
            ticket.setTitulo(ticketDTO.getTitulo());
        }
        if (ticketDTO.getDescripcion() != null) {
            ticket.setDescripcion(ticketDTO.getDescripcion());
        }
        if (ticketDTO.getEstado() != null) {
            try {
                ticket.setEstado(EstadoTicket.valueOf(ticketDTO.getEstado()));
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
    public ComentarioDTO agregarComentario(Long ticketId, ComentarioDTO comentarioDTO, String autor) {
        logger.info("datos recibidos - ticketId: {}, autor: {}, contenido: {}", ticketId, autor,
                comentarioDTO.getContenido());

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("ticket no encontrado"));

        if (comentarioDTO.getContenido() == null || comentarioDTO.getContenido().trim().isEmpty()) {
            throw new BadRequestException("El contenido del comentario no puede estar vacio");
        }

        // crear y guardar comentario
        Comentario comentario = new Comentario();
        comentario.setContenido(comentarioDTO.getContenido());
        comentario.setAutor(autor);
        // comentario.setFechaCreacion(LocalDateTime.now());
        comentario.setTicket(ticket);

        Comentario comentarioGuardado = comentarioRepository.save(comentario);

        if (ticket.getComentarios() == null) {
            ticket.setComentarios(new ArrayList<>());
        }
        ticket.getComentarios().add(comentarioGuardado);

        // Actualizar estado si esta ABIERTO
        if (ticket.getEstado() == EstadoTicket.ABIERTO) {
            ticket.setEstado(EstadoTicket.EN_PROGRESO);
            ticket.setFechaActualizacion(new Date());
        }

        ticketRepository.save(ticket);

        // Notificar al creador del ticket o a los admins
        if (autor.equals(ticket.getCreadoPor().getEmail())) {
            // Si el usuario comenta, notificar a los admins
            notificationService.notifyAdmins("NUEVO_COMENTARIO en Ticket #" + ticket.getId() + " por " + autor);
        } else {
            // Si es un admin (u otro) el que comenta, notificar al creador
            notificationService.notifyUser(ticket.getCreadoPor().getEmail(),
                    "NUEVO_COMENTARIO en tu Ticket #" + ticket.getId());
        }

        return convertComentarioToDTO(comentarioGuardado);

    }

    public Ticket cambiarEstadoTicket(Long Id, String nuevoEstado) {
        Ticket ticket = ticketRepository.findById(Id)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));
        List<String> estadosPermitidos = Arrays.asList("ABIERTO", "EN_PROGRESO", "RESUELTO");
        if (!estadosPermitidos.contains(nuevoEstado)) {
            throw new IllegalArgumentException("Estado no válido: " + nuevoEstado);
        }

        ticket.setEstado(EstadoTicket.valueOf(nuevoEstado));
        ticket.setFechaActualizacion(new Date());

        Ticket updatedTicket = ticketRepository.save(ticket);

        // Notificar al usuario sobre el cambio de estado
        notificationService.notifyUser(updatedTicket.getCreadoPor().getEmail(),
                "ESTADO_ACTUALIZADO: Tu ticket #" + updatedTicket.getId() + " ahora está " + nuevoEstado);

        return updatedTicket;

    }

    public List<ComentarioDTO> obtenerComentariosPorTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new NotFoundException("Ticket no encontrado");
        }

        // obtener los comentarios directamente del repositorio
        List<Comentario> comentarios = comentarioRepository.findByTicketId(ticketId);

        return comentarios.stream()
                .map(this::convertComentarioToDTO)
                .collect(Collectors.toList());
    }

    public void cambiarEstado(Long ticketId, String nuevoEstado) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        ticket.setEstado(EstadoTicket.valueOf(nuevoEstado));
        ticketRepository.save(ticket);
    }
}