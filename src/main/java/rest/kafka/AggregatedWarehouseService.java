package rest.kafka;

import org.springframework.stereotype.Service;
import rest.model.ProductData;
import rest.model.WarehouseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AggregatedWarehouseService {

    private final Map<String, WarehouseData> warehouseDataMap = new HashMap<>();
    private final Object lock = new Object();

    public void addWarehouseData(WarehouseData data) {
        synchronized (lock) {
            warehouseDataMap.put(data.getWarehouseID(), data);
        }
    }

    public List<WarehouseData> getAllWarehouseData() {
        synchronized (lock) {
            return new ArrayList<>(warehouseDataMap.values());
        }
    }

    public WarehouseData getAggregatedData() {
        synchronized (lock) {
            WarehouseData aggregated = new WarehouseData();
            aggregated.setWarehouseID("ALL");
            aggregated.setWarehouseName("All Warehouses");
            
            List<ProductData> allProducts = new ArrayList<>();
            Map<String, ProductData> productMap = new HashMap<>();
            
            for (WarehouseData warehouse : warehouseDataMap.values()) {
                if (warehouse.getProductDataList() != null) {
                    for (ProductData product : warehouse.getProductDataList()) {
                        String key = product.getProductID() + "_" + product.getProductName();
                        if (productMap.containsKey(key)) {
                            ProductData existing = productMap.get(key);
                            existing.setProductQuantity(existing.getProductQuantity() + product.getProductQuantity());
                        } else {
                            ProductData newProduct = new ProductData(
                                product.getProductID(),
                                product.getProductName(),
                                product.getProductCategory(),
                                product.getProductQuantity(),
                                product.getProductUnit()
                            );
                            productMap.put(key, newProduct);
                        }
                    }
                }
            }
            
            allProducts.addAll(productMap.values());
            aggregated.setProductDataList(new ArrayList<>(allProducts));
            
            return aggregated;
        }
    }
}

