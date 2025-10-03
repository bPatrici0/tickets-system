package com.ticket.service;

import com.ticket.entity.Usuario;
import com.ticket.exception.NotFoundException;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodosLosUsuarios() {
        System.out.println("UsuarioService.obtenerTodosLosUsuarios() llamado");
        List<Usuario> usuarios = usuarioRepository.findAll();
        System.out.println("Usuarios en BD: " + usuarios.size());
        return usuarios;
    }

    public Usuario cambiarRolUsuario(Long id, String nuevoRol) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!nuevoRol.equals("ROLE_ADMIN") && !nuevoRol.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Rol inválido: " + nuevoRol);
        }

        usuario.setRol(nuevoRol);
        return usuarioRepository.save(usuario);
    }

    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    public Usuario reiniciarPassword(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        //Establecer una constraseña temporal
        String passwordTemporal = "12345";
        usuario.setPassword(passwordEncoder.encode(passwordTemporal));

        //marcar que debe cambiar la contraseña en el proximo login
        usuario.setPasswordResetRequired(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario cambiarPassword(Long usuarioId, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Uusario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setPasswordResetRequired(false); //ya no requiere cambio

        return usuarioRepository.save(usuario);
    }

    //cambiar contraseña por email para uso despues del login
    public Usuario cambiarPasswordPorEmail(String email, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setPasswordResetRequired(false); //ya no requiere cambio

        return usuarioRepository.save(usuario);
    }
}