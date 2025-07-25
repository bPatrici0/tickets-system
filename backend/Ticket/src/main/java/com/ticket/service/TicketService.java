package com.ticket.service;

import com.ticket.dto.TicketDTO;
import com.ticket.entity.Ticket;
import com.ticket.entity.Usuario;
import com.ticket.exception.NotFoundException;
import com.ticket.repository.TicketRepository;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Ticket crearTicket(TicketDTO ticketDTO) {
        Usuario usuario = usuarioRepository.findByEmail(ticketDTO.getEmailUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ticket ticket = new Ticket();
        ticket.setTitulo(ticketDTO.getTitulo());
        ticket.setDescripcion(ticketDTO.getDescripcion());
        ticket.setEstado(Ticket.EstadoTicket.valueOf(ticketDTO.getEstado()));
        ticket.setCreadoPor(usuario);

        return ticketRepository.save(ticket);
    }

    public List<Ticket> obtenerTodosLosTickets() {
        return ticketRepository.findAll();
    }

    public Ticket obtenerTicketPorId(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));
    }

    public List<Ticket> obtenerTicketsPorUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return ticketRepository.findByCreadoPor(usuario);
    }

    public Ticket actualizarTicket(Long id, TicketDTO ticketDTO) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado"));

        ticket.setTitulo(ticketDTO.getTitulo());
        ticket.setDescripcion(ticketDTO.getDescripcion());
        ticket.setEstado(Ticket.EstadoTicket.valueOf(ticketDTO.getEstado()));

        return ticketRepository.save(ticket);
    }

    public void eliminarTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new NotFoundException("Ticket no encontrado");
        }
        ticketRepository.deleteById(id);
    }
}