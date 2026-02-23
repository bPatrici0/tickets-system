package com.ticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DatabaseExportService {
    @Value("${SPRING_DATASOURCE_URL}")
    private String datasourceUrl;

    @Value("${SPRING_DATASOURCE_USERNAME}")
    private String dbUser;

    @Value("${SPRING_DATASOURCE_PASSWORD}")
    private String dbPassword;

    private String[] parseConnectionDetails() {
        String cleanUrl = datasourceUrl.replace("jdbc:postgresql://", "");
        String host = cleanUrl.split(":")[0];
        String postPart = cleanUrl.split(":")[1];
        String port = postPart.split("/")[0];
        String dbName = postPart.split("/")[1];
        return new String[]{host, port, dbName};
    }

    public String exportDatabase() {
        log.info("Iniciando exportación de base de datos...");

        try {
            String[] conn = parseConnectionDetails();

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", conn[0],
                    "-p", conn[1],
                    "-U", dbUser,
                    "-d", conn[2]);

            pb.environment().put("PGPASSWORD", dbPassword);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String sqlOutput = reader.lines().collect(Collectors.joining("\n"));
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("Exportación completada exitosamente.");
                    return sqlOutput;
                } else {
                    log.error("Error al ejecutar pg_dump. Código de salida : {}", exitCode);
                    throw new RuntimeException("Error en pg_dump");
                }
            }
        } catch (Exception e) {
            log.error("Fallo crítico en la exportación: {}", e.getMessage());
            throw new RuntimeException("No se pudo generar el backup de la base de datos!!!...");
        }
    }

    public String importDatabase(String sqlContent) {
        log.info("Iniciando importación de base de datos...");

        try {
            String[] conn = parseConnectionDetails();

            ProcessBuilder pb = new ProcessBuilder(
                    "psql",
                    "-h", conn[0],
                    "-p", conn[1],
                    "-U", dbUser,
                    "-d", conn[2]);

            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Enviar el contenido SQL al stdin de psql
            try (OutputStream os = process.getOutputStream()) {
                os.write(sqlContent.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // Leer la salida de psql
            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().collect(Collectors.joining("\n"));
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Importación completada exitosamente.");
                return "Importación completada exitosamente.";
            } else {
                log.error("Error al ejecutar psql. Código de salida: {}. Salida: {}", exitCode, output);
                throw new RuntimeException("Error en psql: " + output);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Fallo crítico en la importación: {}", e.getMessage());
            throw new RuntimeException("No se pudo restaurar el backup de la base de datos: " + e.getMessage());
        }
    }
}
