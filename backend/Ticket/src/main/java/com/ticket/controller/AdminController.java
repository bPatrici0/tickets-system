package com.ticket.controller;

import com.ticket.entity.Usuario;
import com.ticket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.ticket.repository.UsuarioRepository;
import com.ticket.dto.RegistroDTO;
import org.springframework.security.crypto.password.PasswordEncoder;

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
            usuarioRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario");
        }
    }
}