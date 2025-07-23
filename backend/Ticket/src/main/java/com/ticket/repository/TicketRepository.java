package com.ticket.repository;

import com.ticket.entity.Ticket;
import com.ticket.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreadoPor(Usuario usuario);
}