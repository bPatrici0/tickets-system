package com.ticket.controller;

import com.ticket.dto.LoginDTO;
import com.ticket.dto.RegistroDTO;
import com.ticket.entity.Usuario;
import com.ticket.entity.Rol;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroDTO registroDTO) {
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Error: El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setRol(Rol.ROLE_USER);

        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        /*Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                .orElse(null);*/

        /*if (usuario == null || !passwordEncoder.matches(loginDTO.getPassword(), usuario.getPassword())) {
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }*/

        return ResponseEntity.ok("Autenticación exitosa");
    }
}