package com.ticket.service;

import com.ticket.dto.ComentarioDTO;
import com.ticket.dto.TicketDTO;
import com.ticket.entity.Ticket;
import com.ticket.entity.Usuario;
import com.ticket.entity.EstadoTicket;
import com.ticket.entity.PrioridadTicket;
import com.ticket.exception.BadRequestException;
import com.ticket.repository.TicketRepository;
import com.ticket.repository.UsuarioRepository;
import com.ticket.repository.ComentarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void crearTicket_Exito() {
        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setTitulo("Error en el servidor");
        ticketDTO.setDescripcion("El servidor se reincia solo");
        ticketDTO.setPrioridad("ALTA");

        Usuario usuario = new Usuario();
        usuario.setEmail("admin@devops.com");

        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.getName()).thenReturn("admin@devops.com");

        when(usuarioRepository.findByEmail("admin@devops.com")).thenReturn(Optional.of(usuario));

        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArguments()[0]);

        Ticket resultado = ticketService.crearTicket(ticketDTO);

        assertNotNull(resultado);
        assertEquals("Error en el servidor", resultado.getTitulo());
        assertEquals(PrioridadTicket.ALTA, resultado.getPrioridad());

        verify(ticketRepository, times(1)).save(any(Ticket.class));

        verify(auditoriaService).registrarAccion(any(), any(), eq("CREACION"), any(), any(), any());
    }

    @Test
    void agregarComentario_AdminCambiaEstadoAInProgress() {
        Long ticketId = 1L;
        String emailAdmin = "admin@devops.com";

        Ticket ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setEstado(EstadoTicket.ABIERTO);

        Usuario creador = new Usuario();
        creador.setEmail("usuario@test.com");
        ticket.setCreadoPor(creador);

        Usuario admin = new Usuario();
        admin.setEmail(emailAdmin);
        admin.setRol("ROLE_ADMIN");

        ComentarioDTO nuevoComentario = new ComentarioDTO();
        nuevoComentario.setContenido("Ya lo estoy revisando");

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        when(usuarioRepository.findByEmail(emailAdmin)).thenReturn(Optional.of(admin));

        when(comentarioRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ticketService.agregarComentario(ticketId, nuevoComentario, emailAdmin);

        assertEquals(EstadoTicket.EN_PROGRESO, ticket.getEstado());

        verify(ticketRepository).save(ticket);

        verify(notificationService).notifyUser(eq("usuario@test.com"), anyString());

        verify(auditoriaService).registrarAccion(eq(ticketId),
                eq(emailAdmin), eq("NUEVO_COMENTARIO"), any(), any(), any());
    }

    @Test
    void crearTicket_ErrorTituloVacio() {

        TicketDTO ticketDTO = new TicketDTO();
        ticketDTO.setTitulo("");

        assertThrows(BadRequestException.class, () -> {
            ticketService.crearTicket(ticketDTO);
        });

        verify(ticketRepository, never()).save(any(Ticket.class));
    }
}