package com.ticket.repository;

import com.ticket.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByTicketIdOrderByFechaDesc(Long ticketId);

    List<Auditoria> findTop20ByOrderByFechaDesc();
}
