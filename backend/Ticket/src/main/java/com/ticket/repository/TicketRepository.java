package com.ticket.repository;

import com.ticket.entity.Ticket;
import com.ticket.entity.Usuario;
import com.ticket.entity.Ticket.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.stream.Collectors;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreadoPor(Usuario usuario);
    List<Ticket> findByEstado(EstadoTicket estado);
    List<Ticket> findByCreadoPorEmailAndEstado(String email, EstadoTicket estado);
    List<Ticket> findByCreadoPorEmail(String email);
}