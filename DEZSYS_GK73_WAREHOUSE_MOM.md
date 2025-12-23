# DEZSYS_GK73_WAREHOUSE_MOM

## Aufgabenstellung

Es soll ein verteiltes Warenlagersystem umgesetzt werden, bei dem mehrere Lagerstandorte ihre Lagerdaten regelmäßig über Apache Kafka (JMS/MOM) an eine Zentrale senden. Die Zentrale sammelt, protokolliert und fasst die Daten zusammen und stellt sie über eine REST-Schnittstelle (JSON/XML) bereit sowie sendet eine Erfolgsbestätigung an die Lagerstandorte zurück.

## Fragen

- Nennen Sie mindestens 4 Eigenschaften der Message Oriented Middleware?
  
  Asynchrone Kommunikation
  
  Zeitliche und räumliche Entkopplung
  
  Zuverlässige Nachrichtenübertragung
  
  Skalierbarkeit / Erweiterbarkeit

- Was versteht man unter einer transienten und synchronen Kommunikation?
  
  *Transient:* Nachricht geht verloren, wenn Empfänger nicht verfügbar
  
  *Synchron:* Sender wartet auf direkte Antwort

- Beschreiben Sie die Funktionsweise einer JMS Queue?
  
  Point-to-Point-Modell
  
  Eine Nachricht hat genau ein Consumer, Nachricht wird nach Empfang gelöscht

- JMS Overview - Beschreiben Sie die wichtigsten JMS Klassen und deren Zusammenhang?
  
  ConnectionFactory → erstellt Verbindungen
  
  Connection → Verbindung zum Broker
  
  Session → Sende-/Empfangskontext
  
  Destination (Queue/Topic)
  
  MessageProducer / MessageConsumer

- Beschreiben Sie die Funktionsweise eines JMS Topic?
  
  Publish-Subscribe-Modell
  
  Eine Nachricht hat mehrere Subscriber
  
  Optional mit dauerhaften Subscriptions

- Was versteht man unter einem lose gekoppelten verteilten System? Nennen Sie ein Beispiel dazu. Warum spricht man hier von lose?
  Komponenten unabhängig voneinander,Keine direkte Kenntnis über Implementierung, „Lose“, weil Systeme getrennt deploybar und austauschbar sind

## Implementierung

Ich habe eine `config.java` hinzugefügt, die die Konfiguration für den Producer und Consumer übernimmt. 

```java
@Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
```

```java
@Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "warehouse-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }
```

### WarehouseKafkaProducer

Dann habe ich einen `WarehouseKafkaProducer.java`, diese Klassen sendt die Daten und schreibt auch noch ein LogFile.

```java
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
```

### MessageConsumer

Ist die Zentrale, die die Nachrichten aus Kafka entgegennimmt, speichert und ein Feedback („SUCCESS“) zurückschickt.

Mit dem Objekt Mapper werden die Json in WarehouseData umgewandelt.

```java
  private void processMessage(String content, String warehouseId) {
      WarehouseData data = mapper.readValue(content, WarehouseData.class);
      aggregatedService.addWarehouseData(data);
      logMessage("RECEIVED", warehouseId, content);
      sendSuccessFeedback(warehouseId);
  }
```

Nach einen erfolgreichen Dateaustausch wird ein Success geschickt und ein LogFile geschrieben.

```java
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
```

### Controller

Beim Controller habe ich noch eine Rest-Schnittstelle hinzugefügt, die bei dem Aufruf von `/warehouse/send` die Daten Message Queues schickt.

```java
 @PostMapping(value = "/warehouse/send")
    public ResponseEntity<String> sendWarehouseData(
            @RequestParam(required = false) String warehouseID,
            @RequestParam(required = false) String location) {

        String id = warehouseID != null ? warehouseID : "001";
        String loc = location != null ? location : "Linz";

        WarehouseData data = service.getWarehouseData(id, loc);
        kafkaProducer.sendWarehouseData(data);

        return ResponseEntity.ok("Warehouse data sent to message queue");
    }
```
