package com.ticket.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    public String exportDatabase() {
        log.info("Iniciando exportación de base de datos...");

        try {
            String cleanUrl = datasourceUrl.replace("jdbc:postgresql://", "");
            String host = cleanUrl.split(":")[0];
            String postPart = cleanUrl.split(":")[0];
            String port = postPart.split("/")[0];
            String dbName = postPart.split("/")[1];

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", host,
                    "-p", port,
                    "-u", dbUser,
                    "-d", dbName);

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
}