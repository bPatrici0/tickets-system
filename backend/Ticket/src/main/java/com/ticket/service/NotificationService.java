package com.ticket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación a todos los administradores.
     */
    public void notifyAdmins(String message) {
        log.info(">>> Enviando notificación a admins: {}", message);
        messagingTemplate.convertAndSend("/topic/admins", Objects.requireNonNull(message));
    }

    /**
     * Envía una notificación a un usuario específico por su email.
     */
    public void notifyUser(String email, String message) {
        log.info(">>> Enviando notificación a usuario {}: {}", email, message);
        messagingTemplate.convertAndSendToUser(Objects.requireNonNull(email), "/queue/notifications",
                Objects.requireNonNull(message));
    }

    /**
     * Notificación general de tickets.
     */
    public void broadcastTicketUpdate(String message) {
        log.info(">>> Broadcasting ticket update: {}", message);
        messagingTemplate.convertAndSend("/topic/tickets", Objects.requireNonNull(message));
    }
}
