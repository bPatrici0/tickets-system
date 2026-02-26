package com.ticket.controller;

import com.ticket.entity.Usuario;
import com.ticket.entity.Ticket;
import com.ticket.service.UsuarioService;
import com.ticket.service.TicketService;
import com.ticket.service.DatabaseExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import com.ticket.repository.UsuarioRepository;
import com.ticket.repository.TicketRepository;
import com.ticket.repository.ComentarioRepository;
import com.ticket.repository.AuditoriaRepository;
import com.ticket.dto.RegistroDTO;
import com.ticket.dto.UsuarioUpdateDTO;
import com.ticket.exception.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private DatabaseExportService databaseExportService;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> obtenerTodosLosUsuarios() {
        log.debug("AdminController.obtenerTodosLosUsuarios() llamado");
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        log.info("Usuarios encontrados: {}", usuarios.size());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> obtenerTodosLosTickets() {
        log.debug("AdminController.obtenerTodosLosTickets() llamado");
        List<Ticket> tickets = ticketService.obtenerTodosLosTickets();
        log.info("Tickets encontrados: {}", tickets.size());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<Ticket> obtenerTicketPorId(@PathVariable Long id) {
        log.debug("AdminController.obtenerTicketPorId() llamado - ID: {}", id);
        try {
            Ticket ticket = ticketService.obtenerTicketPorId(id);

            if (ticket == null) {
                log.warn("Ticket no encontrado - ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            log.debug("Ticket encontrado: {}", ticket.getTitulo());
            return ResponseEntity.ok(ticket);
        } catch (NotFoundException e) {
            log.warn("Ticket no encontrado - ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error obteniendo ticket ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/usuarios/{id}/role")
    public ResponseEntity<Usuario> cambiarRolUsuario(@PathVariable Long id, @RequestBody Map<String, String> request) {
        log.debug("AdminController.cambiarRolUsuario() llamado - ID: {}", id);

        String nuevoRol = request.get("role");
        if (nuevoRol == null) {
            log.warn("Rol no proporcionado para usuario ID: {}", id);
            return ResponseEntity.badRequest().build();
        }

        log.info("Cambiando rol de usuario {} a: {}", id, nuevoRol);
        return ResponseEntity.ok(usuarioService.cambiarRolUsuario(id, nuevoRol));
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        log.debug("AdminController.obtenerUsuarioPorId() llamado - ID: {}", id);
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody RegistroDTO registroDTO) {
        log.info("AdminController.crearUsuario() llamado para email: {}", registroDTO.getEmail());

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
        log.info("AdminController.eliminarUsuario() llamado - ID: {}", id);

        if (id == 1) {
            log.warn("Intento de eliminar super usuario - Acceso Denegado");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: El Super Usuario no pued ser eliminado del sistema!!!...");
        }

        try {
            if (!usuarioRepository.existsById(id)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            usuarioRepository.deleteById(id);
            log.info("Usuario eliminado existosamente - ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error al eliminar usuario ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar usuario");
        }
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioUpdateDTO updateDTO) {
        log.info("Actualizando usuario ID: {}", id);

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
        log.debug("Cambiando estado usuario ID: {}", id);

        if (id == 1) {
            log.warn("Intento de desactivar  al Super Usuario - Acesso Denegado!!!...");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: El Super Usuario no puede ser desactivado!!!...");
        }

        Boolean activo = request.get("activo");
        if (activo == null) {
            log.warn("Error: campo 'activo' no encontrado en request para usuario ID: {}", id);
            return ResponseEntity.badRequest().body("El campo 'activo' es requerido");
        }

        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            log.info("Cambiando estado de usuario {} de {} a {}", usuario.getEmail(), usuario.getActivo(), activo);

            usuario.setActivo(activo);
            Usuario usuarioActualizado = usuarioRepository.save(usuario);

            return ResponseEntity.ok(usuarioActualizado);

        } catch (NotFoundException e) {
            log.warn("Usuario no encontrado ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al cambiar estado de usuario ID {}: {}", id, e.getMessage());
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

    @PostMapping("/usuarios/{id}/reiniciar-password")
    public ResponseEntity<?> reiniciarPassword(@PathVariable Long id) {
        try {
            Usuario usuarioActualizado = usuarioService.reiniciarPassword(id);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Contraseña reiniciada correctamente",
                    "usuario", usuarioActualizado.getEmail(),
                    "passwordResetRequired", usuarioActualizado.getPasswordResetRequired()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al reiniciar contraseña: " + e.getMessage());
        }
    }

    @GetMapping("/db/export")
    public ResponseEntity<String> exportarBaseDeDatos() {
        log.info("AdminController: Solicitada exportación de BBDD");

        try {
            String sqlDump = databaseExportService.exportDatabase();
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=backup_db_" +
                            System.currentTimeMillis() + ".sql")
                    .header("Content-Type", "application/sql")
                    .body(sqlDump);
        } catch (Exception e) {
            log.error("Error al exportar BBDD: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("-- ERROR AL GENERAR BACKUP: " + e.getMessage());
        }
    }

    @PostMapping("/db/import")
    public ResponseEntity<String> importarBaseDeDatos(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        log.info("AdminController: Solicitada importación de BBDD - Archivo: {}", file.getOriginalFilename());

        try {
            // Validar que sea un archivo .sql
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".sql")) {
                return ResponseEntity.badRequest()
                        .body("Error: Solo se aceptan archivos con extensión .sql");
            }

            // Validar que no esté vacío
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Error: El archivo está vacío.");
            }

            String sqlContent = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String resultado = databaseExportService.importDatabase(sqlContent);

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al importar BBDD: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al restaurar la base de datos: " + e.getMessage());
        }
    }

    @PostMapping("/db/purge")
    public ResponseEntity<String> purgarSistema(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        log.warn("⚠️  SOLICITUD DE PURGA DEL SISTEMA por: {}", email);

        // Solo admin@devops.com puede ejecutar esta operación
        if (!"admin@devops.com".equals(email)) {
            log.warn("Intento de purga rechazado: usuario no autorizado ({})", email);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: Solo admin@devops.com puede ejecutar esta operación.");
        }

        // Verificar contraseña
        Usuario admin = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario admin@devops.com no encontrado."));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            log.warn("Intento de purga rechazado: contraseña incorrecta para {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error: Contraseña incorrecta.");
        }

        try {
            log.warn("🔴 EJECUTANDO PURGA TOTAL DEL SISTEMA...");

            // Orden: comentarios → tickets → auditoría (respetar FKs)
            long comentarios = comentarioRepository.count();
            comentarioRepository.deleteAll();
            log.info("Eliminados {} comentarios.", comentarios);

            long tickets = ticketRepository.count();
            ticketRepository.deleteAll();
            log.info("Eliminados {} tickets.", tickets);

            long auditorias = auditoriaRepository.count();
            auditoriaRepository.deleteAll();
            log.info("Eliminados {} registros de auditoría.", auditorias);

            String resumen = String.format(
                    "Purga completada: %d comentarios, %d tickets, %d registros de auditoría eliminados.",
                    comentarios, tickets, auditorias);

            log.warn("✅ PURGA COMPLETADA: {}", resumen);
            return ResponseEntity.ok(resumen);

        } catch (Exception e) {
            log.error("Error crítico durante la purga: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error durante la purga del sistema: " + e.getMessage());
        }
    }
}
