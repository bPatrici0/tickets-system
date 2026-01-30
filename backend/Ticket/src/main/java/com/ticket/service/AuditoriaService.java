package com.ticket.service;

import com.ticket.entity.Auditoria;
import com.ticket.repository.AuditoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Transactional
    public void registrarAccion(Long ticketId, String usuario, String accion, String valorAnterior, String valorNuevo,
            String detalles) {
        Auditoria registro = new Auditoria();
        registro.setTicketId(ticketId);
        registro.setUsuario(usuario);
        registro.setAccion(accion);
        registro.setValorAnterior(valorAnterior);
        registro.setValorNuevo(valorNuevo);
        registro.setDetalles(detalles);
        registro.setFecha(LocalDateTime.now());

        auditoriaRepository.save(registro);
    }

    public List<Auditoria> obtenerHistorialPorTicket(Long ticketId) {
        return auditoriaRepository.findByTicketIdOrderByFechaDesc(ticketId);
    }

}
