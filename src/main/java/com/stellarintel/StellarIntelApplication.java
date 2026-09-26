package com.stellarintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * StellarIntel Application Entry Point.
 */
@SpringBootApplication
public class StellarIntelApplication {

    public static void main(String[] args) {
        loadLocalEnvironmentFile();
        SpringApplication.run(StellarIntelApplication.class, args);
    }

    /**
     * Loads key-value pairs from a local .env file in the working directory
     * into JVM System properties if not already defined.
     */
    private static void loadLocalEnvironmentFile() {
        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int eqIdx = line.indexOf('=');
                        String key = line.substring(0, eqIdx).trim();
                        String val = line.substring(eqIdx + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            if (val.length() >= 2) {
                                val = val.substring(1, val.length() - 1);
                            }
                        }
                        if (!key.isEmpty() && System.getProperty(key) == null) {
                            System.setProperty(key, val);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
