package com.ticket.service;

import com.ticket.entity.Usuario;
import com.ticket.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.ticket.entity.Rol;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.ticket.service.CustomUserDetailsService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String authority = usuario.getRol().name().startsWith("ROLE_")
                ? usuario.getRol().name()
                : "ROLE_" + usuario.getRol().name();

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}