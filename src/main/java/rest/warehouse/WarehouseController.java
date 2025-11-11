package rest.warehouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import rest.model.ProductData;
import rest.model.WarehouseData;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class WarehouseController {

    @Autowired
    private WarehouseService service;


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