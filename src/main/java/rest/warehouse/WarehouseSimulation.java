package rest.warehouse;

import rest.model.ProductData;
import rest.model.WarehouseData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;

public class WarehouseSimulation {
    private final Random rand = new Random();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private double getRandomDouble(int inMinimum, int inMaximum) {

        double number = (Math.random() * ((inMaximum - inMinimum) + 1)) + inMinimum;
        double rounded = Math.round(number * 100.0) / 100.0;
        return rounded;

    }

    private int getRandomInt(int inMinimum, int inMaximum) {

        double number = (Math.random() * ((inMaximum - inMinimum) + 1)) + inMinimum;
        Long rounded = Math.round(number);
        return rounded.intValue();

    }

    public WarehouseData getData(String warehouseID, String warehouseName) {
        WarehouseData warehouse = new WarehouseData();
        warehouse.setWarehouseID(warehouseID);
        warehouse.setWarehouseName(warehouseName);
        warehouse.setTimestamp(LocalDateTime.now().format(formatter));
        ArrayList<ProductData> productData = new ArrayList<>();
        productData.add(new ProductData("0", "Milch Vollmilch", "Milch", 500, "0.5L"));
        productData.add(new ProductData("1", "Bio Apfelsaft", "Saft", 200, "1.5L"));
        productData.add(new ProductData("3", "Vöslauer Wasser", "Wasser", 200, "1L"));
        productData.add(new ProductData("4", "Römerquelle", "Wasser", 20, "3L"));
        productData.add(new ProductData("5", "Bio Orangensaft", "Saft", 100, "0.5L"));
        productData.add(new ProductData("6", "Ariel Waschmittel Color", "Waschmittel", 200, "3L"));
        productData.add(new ProductData("7", "Persil Discs Color", "Waschmittel", 10, "10kg"));

        int randomZahl = getRandomInt(0, 6);
        ArrayList<ProductData> productData2 = new ArrayList<>();
        for (int i = 0; i < randomZahl; i++) {
            productData2.add(productData.get(i));
        }
        warehouse.setProductDataList(productData2);
        return warehouse;
    }
}



