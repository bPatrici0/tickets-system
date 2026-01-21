package com.ticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilitar un broker simple para enviar mensajes a los clientes
        config.enableSimpleBroker("/topic", "/queue");
        // Prefijo para los mensajes que van desde el cliente al servidor
        // (@MessageMapping)
        config.setApplicationDestinationPrefixes("/app");
        // Prefijo para mensajes dirigidos a usuarios específicos
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint de conexión para el cliente
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permitir todos los orígenes para desarrollo
                .withSockJS(); // Soporte para navegadores que no soportan WebSockets
    }
}
