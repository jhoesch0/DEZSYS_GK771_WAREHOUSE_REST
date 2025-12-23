package rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import rest.kafka.AggregatedWarehouseService;
import rest.model.WarehouseData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final AggregatedWarehouseService aggregatedService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String LOG_FILE = "warehouse_receive.log";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public MessageConsumer(AggregatedWarehouseService aggregatedService, KafkaTemplate<String, String> kafkaTemplate) {
        this.aggregatedService = aggregatedService;
        this.kafkaTemplate = kafkaTemplate;
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

    @KafkaListener(topics = "warehouse-queue-001", groupId = "warehouse-consumer-group")
    public void processMessage001(String content) {
        processMessage(content, "001");
    }

    @KafkaListener(topics = "warehouse-queue-002", groupId = "warehouse-consumer-group")
    public void processMessage002(String content) {
        processMessage(content, "002");
    }

    @KafkaListener(topics = "warehouse-queue-003", groupId = "warehouse-consumer-group")
    public void processMessage003(String content) {
        processMessage(content, "003");
    }

    private void processMessage(String content, String warehouseId) {
        try {
            WarehouseData data = mapper.readValue(content, WarehouseData.class);
            logger.info("Received warehouse data from queue: warehouse-queue-{}", warehouseId);
            System.out.println("Read from Message Queue warehouse-queue-" + warehouseId + ": " + data);
            
            aggregatedService.addWarehouseData(data);
            logMessage("RECEIVED", warehouseId, content);
            
            sendSuccessFeedback(warehouseId);
            
        } catch (Exception e) {
            logger.error("Error processing message from warehouse-queue-{}", warehouseId, e);
            e.printStackTrace();
        }
    }

    private void sendSuccessFeedback(String warehouseId) {
        try {
            String feedbackTopic = "warehouse-feedback-" + warehouseId;
            kafkaTemplate.send(feedbackTopic, "SUCCESS");
            logger.info("Sent SUCCESS feedback to topic: {}", feedbackTopic);
        } catch (Exception e) {
            logger.error("Error sending success feedback", e);
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

