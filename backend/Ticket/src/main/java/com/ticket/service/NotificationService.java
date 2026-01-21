package com.ticket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación a todos los administradores.
     */
    public void notifyAdmins(String message) {
        messagingTemplate.convertAndSend("/topic/admins", message);
    }

    /**
     * Envía una notificación a un usuario específico por su email.
     */
    public void notifyUser(String email, String message) {
        messagingTemplate.convertAndSendToUser(email, "/queue/notifications", message);
    }

    /**
     * Notificación general de tickets.
     */
    public void broadcastTicketUpdate(String message) {
        messagingTemplate.convertAndSend("/topic/tickets", message);
    }
}
