package com.ticket.controller;

import com.ticket.dto.LoginDTO;
import com.ticket.dto.LoginResponseDTO;
import com.ticket.dto.RegistroDTO;
import com.ticket.dto.ChangePasswordDTO;
import com.ticket.entity.Usuario;
import com.ticket.entity.Rol;
import com.ticket.repository.UsuarioRepository;
import com.ticket.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroDTO registroDTO) {
        log.info("Intento de registro para email: {}", registroDTO.getEmail());

        Usuario usuario = new Usuario();
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setRol(registroDTO.getRol() != null ? registroDTO.getRol() : "ROLE_USER");
        usuario.setNombre(registroDTO.getNombre());
        usuario.setPasswordResetRequired(false);

        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Usuario registrado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        try {
            //autenticar
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            //obtener usario de la bd
            Usuario usuario = usuarioRepository.findByEmail(loginDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado!..."));

            //respuesta con datos necesarios
            return ResponseEntity.ok(new LoginResponseDTO(
                    usuario.getEmail(),
                    usuario.getRol(),
                    usuario.getPasswordResetRequired(),
                    usuario.getNombre()
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(changePasswordDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuarioService.cambiarPasswordPorEmail(changePasswordDTO.getEmail(), changePasswordDTO.getNuevaPassword());

            return ResponseEntity.ok("Contraseña cambiada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al cambiar constraseña: " + e.getMessage());
        }
    }

    @GetMapping("/requiere-cambio-password/{email}")
    public ResponseEntity<?> requiereCambioPassword(@PathVariable String email) {
        try {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            return ResponseEntity.ok(usuario.getPasswordResetRequired());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al verificar estado de contraseña");
        }
    }
}