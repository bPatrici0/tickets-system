package com.ticket.controller;

import com.ticket.entity.Usuario;
import com.ticket.entity.Rol;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javassist.NotFoundException;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/usuarios/{id}/rol")
    public ResponseEntity<Usuario> cambiarRolUsuario(@PathVariable Long id, @RequestParam Rol nuevoRol) throws NotFoundException {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(usuario);
    }
}