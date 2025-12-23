package rest.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import rest.model.WarehouseData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WarehouseKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseKafkaProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String LOG_FILE = "warehouse_send.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public WarehouseKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        initializeLogFile();
    }

    private void initializeLogFile() {
        try {
            if (!Files.exists(Paths.get(LOG_FILE))) {
                Files.createFile(Paths.get(LOG_FILE));
            }
        } catch (IOException e) {
            logger.error("Error initializing log file", e);
        }
    }

    public void sendWarehouseData(WarehouseData warehouseData) {
        try {
            String warehouseId = warehouseData.getWarehouseID();
            String topicName = "warehouse-queue-" + warehouseId;
            
            String jsonData = objectMapper.writeValueAsString(warehouseData);
            
            kafkaTemplate.send(topicName, jsonData);
            
            logMessage("SENT", warehouseId, jsonData);
            logger.info("Sent warehouse data to topic: {} for warehouse: {}", topicName, warehouseId);
            
        } catch (Exception e) {
            logger.error("Error sending warehouse data to Kafka", e);
            throw new RuntimeException("Failed to send warehouse data", e);
        }
    }

    private void logMessage(String action, String warehouseId, String data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            String timestamp = LocalDateTime.now().format(formatter);
            writer.println(String.format("[%s] %s - WarehouseID: %s - Data: %s", 
                timestamp, action, warehouseId, data));
        } catch (IOException e) {
            logger.error("Error writing to log file", e);
        }
    }
}

