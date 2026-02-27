package com.altynai.attendance;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AttendanceApplication {

    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .ignoreIfMalformed()
                    .load();
            String mongoUri = dotenv.get("MONGO_URI");
            if (mongoUri != null && !mongoUri.isBlank()) {
                System.setProperty("MONGO_URI", mongoUri);
            }
        } catch (Exception ignored) {
            // Fallback to env vars / application.properties placeholder resolution.
        }

        SpringApplication.run(AttendanceApplication.class, args);
    }
}
