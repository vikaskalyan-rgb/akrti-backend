package com.akriti.apartment.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseKeepAlive {

    private static final Logger log = LoggerFactory.getLogger(DatabaseKeepAlive.class);

    @Autowired
    private DataSource dataSource;

    // Ping DB every 3 minutes to prevent Neon from suspending
    @Scheduled(fixedRate = 180000)
    public void keepAlive() {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute("SELECT 1");
            log.debug("✅ DB keepalive ping successful");
        } catch (Exception e) {
            log.warn("⚠ DB keepalive failed: {}", e.getMessage());
        }
    }
}