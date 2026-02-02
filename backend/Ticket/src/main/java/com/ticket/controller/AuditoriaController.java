package com.ticket.controller;

import com.ticket.entity.Auditoria;
import com.ticket.dto.PerformanceDTO;
import com.ticket.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    // Obtener el historial completo de un ticket específico
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<Auditoria>> obtenerHistorialTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(auditoriaService.obtenerHistorialPorTicket(ticketId));
    }

    // Obtener los últimos 20 movimientos del sistema (para el Dashboard)
    @GetMapping("/recientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Auditoria>> obtenerUltimosMovimientos() {
        return ResponseEntity.ok(auditoriaService.obtenerUltimosMovimientos());
    }

    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PerformanceDTO>> obtenerRendimientoTecnicos() {
        return ResponseEntity.ok(auditoriaService.obtenerEstadisticasRendimiento());
    }
}
