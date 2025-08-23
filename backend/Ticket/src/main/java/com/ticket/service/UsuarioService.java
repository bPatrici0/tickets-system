package com.ticket.service;

import com.ticket.entity.Usuario;
import com.ticket.exception.NotFoundException;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Usuario> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll();
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
}