package com.ticket.service;

import com.ticket.dto.TicketDTO;
import com.ticket.entity.Ticket;
import com.ticket.entity.Usuario;
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
    public UsuarioRepository usuarioRepository;

    public Ticket crearTicket(TicketDTO ticketDTO) {
        Usuario usuario = usuarioRepository.findByEmail(ticketDTO.getEmailUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Ticket ticket = new Ticket();
        ticket.setTitulo(ticketDTO.getTitulo());
        ticket.setDescripcion(ticketDTO.getDescripcion());
        ticket.setEstado(ticketDTO.getEstado());
        ticket.setCreadoPor(usuario);

        return ticketRepository.save(ticket);
    }

    public List<Ticket> obtenerTicketsPorUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ticketRepository.findByCreadoPor(usuario);
    }

    public List<Ticket> obtenerTodosLosTickets() {
        return ticketRepository.findAll(); // Asegúrate de tener este método en tu repository
    }
}