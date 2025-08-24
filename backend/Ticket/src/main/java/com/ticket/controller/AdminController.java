package com.ticket.controller;

import com.ticket.entity.Usuario;
import com.ticket.entity.Rol;
import com.ticket.service.UsuarioService;
import com.ticket.repository.UsuarioRepository;
import com.ticket.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }

    @PutMapping("/usuarios/{id}/role")
    public ResponseEntity<Usuario> cambiarRolUsuario(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String nuevoRol = request.get("role");
        if (nuevoRol == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(usuarioService.cambiarRolUsuario(id, nuevoRol));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> obtenerUsuariosPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @PostMapping("/usuarios/{id}/rol")
    public ResponseEntity<Usuario> cambiarRolUsuario(@PathVariable Long id, @RequestParam Rol nuevoRol) throws NotFoundException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setRol("ROLE_USER");
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(usuario);
    }
}