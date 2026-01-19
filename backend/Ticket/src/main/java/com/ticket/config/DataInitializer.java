package com.ticket.config;

import com.ticket.entity.Usuario;
import com.ticket.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            log.info("Base de datos vacía. Inicializando datos por defecto...");

            Usuario admin = new Usuario();
            admin.setEmail("admin@devops.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNombre("Administrador");
            admin.setRol("ROLE_ADMIN");
            admin.setPasswordResetRequired(false);

            usuarioRepository.save(admin);
            log.info("Usuario Admin creado correctamente: admin@devops.com / admin123");
        } else {
            log.info("La base de datos ya contiene usuarios. Saltando inicialización.");
        }
    }
}
