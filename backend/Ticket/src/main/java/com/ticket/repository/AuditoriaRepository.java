package com.ticket.repository;

import com.ticket.entity.Auditoria;
import com.ticket.dto.PerformanceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByTicketIdOrderByFechaDesc(Long ticketId);

    List<Auditoria> findTop20ByOrderByFechaDesc();

    @Query("SELECT new com.ticket.dto.PerformanceDTO(a.usuario, COUNT(a)) "
            + "FROM Auditoria a "
            + "WHERE a.accion = 'CAMBIO_ESTADO' AND a.valorNuevo = 'RESUELTO' "
            + "GROUP BY a.usuario "
            + "ORDER BY COUNT(a) DESC")
    List<PerformanceDTO> countResolucionesPorUsuario();
}
