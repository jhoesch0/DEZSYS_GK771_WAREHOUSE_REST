package rest.warehouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rest.kafka.AggregatedWarehouseService;
import rest.kafka.WarehouseKafkaProducer;
import rest.model.ProductData;
import rest.model.WarehouseData;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class WarehouseController {

    @Autowired
    private WarehouseService service;

    @Autowired
    private WarehouseKafkaProducer kafkaProducer;

    @Autowired
    private AggregatedWarehouseService aggregatedService;


    @GetMapping(value = "/warehouse/{inID}/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public WarehouseData warehouseDataJson(
            @PathVariable String inID,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String productName) {

        return filterWarehouseData(inID, location, productName);
    }


    @GetMapping(value = "/warehouse/{inID}/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public WarehouseData warehouseDataXml(
            @PathVariable String inID,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String productName) {

        return filterWarehouseData(inID, location, productName);
    }

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

    @GetMapping(value = "/warehouse/all/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<WarehouseData> getAllWarehouseDataJson() {
        return aggregatedService.getAllWarehouseData();
    }

    @GetMapping(value = "/warehouse/all/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public List<WarehouseData> getAllWarehouseDataXml() {
        return aggregatedService.getAllWarehouseData();
    }

    @GetMapping(value = "/warehouse/aggregated/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public WarehouseData getAggregatedDataJson() {
        return aggregatedService.getAggregatedData();
    }

    @GetMapping(value = "/warehouse/aggregated/xml", produces = MediaType.APPLICATION_XML_VALUE)
    public WarehouseData getAggregatedDataXml() {
        return aggregatedService.getAggregatedData();
    }

    private WarehouseData filterWarehouseData(String inID, String location, String productName) {
        WarehouseData data = service.getWarehouseData(inID, location != null ? location : "Linz");

        if (productName != null && !productName.isEmpty()) {
            List<ProductData> filtered = data.getProductDataList().stream()
                    .filter(p -> p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                    .toList();
            data.setProductDataList(new java.util.ArrayList<>(filtered));
        }

        return data;
    }
}