package rest.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WarehouseFeedbackConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseFeedbackConsumer.class);
    private static final String LOG_FILE = "warehouse_feedback.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public WarehouseFeedbackConsumer() {
        initializeLogFile();
    }

    private void initializeLogFile() {
        try {
            if (!Files.exists(Paths.get(LOG_FILE))) {
                Files.createFile(Paths.get(LOG_FILE));
            }
        } catch (IOException e) {
            logger.error("Error initializing feedback log file", e);
        }
    }

    @KafkaListener(topics = "warehouse-feedback-001", groupId = "warehouse-feedback-group")
    public void receiveFeedback001(String message) {
        processFeedback(message, "001");
    }

    @KafkaListener(topics = "warehouse-feedback-002", groupId = "warehouse-feedback-group")
    public void receiveFeedback002(String message) {
        processFeedback(message, "002");
    }

    @KafkaListener(topics = "warehouse-feedback-003", groupId = "warehouse-feedback-group")
    public void receiveFeedback003(String message) {
        processFeedback(message, "003");
    }

    private void processFeedback(String message, String warehouseId) {
        logger.info("Received feedback for warehouse {}: {}", warehouseId, message);
        System.out.println("Feedback received for warehouse " + warehouseId + ": " + message);
        logFeedback(warehouseId, message);
    }

    private void logFeedback(String warehouseId, String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.println(String.format("[%s] FEEDBACK - WarehouseID: %s - Message: %s", 
                timestamp, warehouseId, message));
        } catch (IOException e) {
            logger.error("Error writing to feedback log file", e);
        }
    }
}

