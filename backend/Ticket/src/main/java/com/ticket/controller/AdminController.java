package com.ticket.controller;

import com.ticket.entity.Usuario;
import com.ticket.entity.Ticket;
import com.ticket.service.UsuarioService;
import com.ticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.ticket.repository.UsuarioRepository;
import com.ticket.dto.RegistroDTO;
import com.ticket.dto.UsuarioUpdateDTO;
import com.ticket.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        System.out.println("AdminController.obtenerTodosLosUsuarios() llamado");
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        System.out.println("Usuarios encontrados: " + usuarios.size());
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/usuarios/{id}/role")
    public ResponseEntity<Usuario> cambiarRolUsuario(@PathVariable Long id, @RequestBody Map<String, String> request) {
        System.out.println("AdminController.cambiarRolUsuario() llamado - ID: " + id);

        String nuevoRol = request.get("role");
        if (nuevoRol == null) {
            System.out.println("Rol no proporcionado");
            return ResponseEntity.badRequest().build();
        }

        System.out.println("Cambiando rol a: " + nuevoRol);
        return ResponseEntity.ok(usuarioService.cambiarRolUsuario(id, nuevoRol));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        System.out.println("AdminController.obtenerUsuarioPorId() llamado - ID: " + id);
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody RegistroDTO registroDTO) {
        System.out.println("AdminController.crearUsuario() llamado");
        System.out.println("Datos: " + registroDTO.getEmail() + ", " + registroDTO.getNombre() +
                "rol: " +registroDTO.getRol());

        Usuario usuario = new Usuario();
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setNombre(registroDTO.getNombre());
        usuario.setRol(registroDTO.getRol());

        usuarioRepository.save(usuario);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        System.out.println("AdminController.eliminarUsuario() llamado - ID: " + id);

        try {
            if (!usuarioRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            usuarioRepository.deleteById(id);
            System.out.println("Usuario eliminado existosamente - ID: " + id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Error al eliminar usuario ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario");
        }
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateDTO updateDTO) {
        System.out.println("Actualizando usuario ID: " + id);

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (updateDTO.getNombre() != null) {
            usuario.setNombre(updateDTO.getNombre());
        }
        if (updateDTO.getEmail() != null) {
            usuario.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getRol() != null) {
            usuario.setRol(updateDTO.getRol());
        }
        if (updateDTO.getActivo() != null) {
            usuario.setActivo(updateDTO.getActivo());
        }

        return ResponseEntity.ok(usuarioRepository.save(usuario));
    }

    @PutMapping("/usuarios/{id}/status")
    public ResponseEntity<?> cambiarEstadoUsuario(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        System.out.println("Cambiando estado usuario ID: " + id);
        System.out.println("Request body: " + request);

        Boolean activo = request.get("activo");
        System.out.println("Valor de 'activo': " + activo);

        if (activo == null) {
            System.out.println("Error: campo 'activo' no encontrado en request");
            return ResponseEntity.badRequest().body("El campo 'activo' es requerido");
        }

        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            System.out.println("Usuario encontrado: " + usuario.getEmail());
            System.out.println("Estado actual: " + usuario.getActivo());
            System.out.println("Nuevo estado: " + activo);

            usuario.setActivo(activo);
            Usuario usuarioActualizado = usuarioRepository.save(usuario);

            System.out.println("Usuario actualizado exitosamente");
            return ResponseEntity.ok(usuarioActualizado);

        } catch (NotFoundException e) {
            System.out.println("Usuario no encontrado ID: " + id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("Error al cambiar estado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar estado");
        }
    }

    @PutMapping("/tickets/{id}/estado")
    public ResponseEntity<Ticket> cambiarEstadoTicket(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String nuevoEstado = request.get("estado");
        Ticket ticket = ticketService.cambiarEstadoTicket(id, nuevoEstado);
        return ResponseEntity.ok(ticket);
    }
}