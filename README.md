# Middleware Engineering "REST and Data Formats"

## Aufgabenstellung

Entwickeln Sie einen Simulator, der die Daten eines Lagerstandortes (WHx) generiert. Es ist dabei zu achten, dass die Daten realistisch sind und im Zusammenhang mit entsprechenden Einheiten erzeugt werden. Diese Daten sollen gemeinsam mit einigen Details zum dem Standort ueber eine REST Schnittstelle veroeffentlicht werden. Die Schnittstelle verwendet standardmaessig das JSON Format und kann optional auf XML umgestellt werden.

## Implementierung

```java
private String productID;
private String productName;
private String productCategory;
private int productQuantity;
private String productUnit;
```

```java
private String warehouseID;
private String warehouseName;
private String timestamp;
private ArrayList<ProductData> productDataList;
```

Auerst habe ich die fehlenden Attribute in den Klassen `WarehouseData` und `ProductData` angepasst.

```java
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
```

Dann habe ich in `WarehouseData` die Methode `getData`geschrieben, diese generiert jedes mal wenn die Seite aktualisiert wird die Produkte neu.

```java
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
```

Außerdem habe ich den RestController neu geschrieben. Diese fragt nach der ID, Format, Standort und dem Produktnamen. Dieser Filtert auch die Liste, wenn etwas eingetragen wird. 

```java
@JacksonXmlRootElement(localName = "warehouseData")
public class WarehouseData {

    private String warehouseID;
    private String warehouseName;
    private String timestamp;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "ProductData")
    private ArrayList<ProductData> productDataList;
```

```groovy
 implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml'
```

Um die XML Daten anzuzeigen, habe ich diese Sachen hinzugefügt. `@JacksonXmlElementWrapper(useWrapping = false)` habe ich benutzt, das alle Produkte in der Liste angezeigt werden.

<img src="file:///C:/Users/jonas/AppData/Roaming/marktext/images/2025-11-11-13-40-59-image.png" title="" alt="" width="284">

XML-Seite

<img src="file:///C:/Users/jonas/AppData/Roaming/marktext/images/2025-11-11-13-51-20-image.png" title="" alt="" width="362">

JSON-Seite

```html
<table id="productTable">
    <thead>
      <tr>
        <th>Produkt-ID</th>
        <th>Produktname</th>
        <th>Kategorie</th>
        <th>Menge</th>
        <th>Einheit</th>
      </tr>
    </thead>
    <tbody></tbody>
  </table>
```

```js
function fillTable(products) {
      const tbody = $('#productTable tbody');
      tbody.empty();
      if (!products || products.length === 0) {
        tbody.append('<tr><td colspan="5">Keine Produkte gefunden</td></tr>');
        return;
      }
      products.forEach(p => {
        tbody.append(`<tr>
          <td>${p.productID}</td>
          <td>${p.productName}</td>
          <td>${p.productCategory}</td>
          <td>${p.productQuantity}</td>
          <td>${p.productUnit}</td>
        </tr>`);
      });
    }
```

Auf der HTML Seite habe ich eine Tabelle definiert und verschiedene Methoden geschrieben, die helfen die Daten anzuzeigen. 

```js
function handleJson(data) {
      showWarehouseInfo(data);
      fillTable(data.productDataList || []);
    }
```

Diese Methode füllt die Tabelle mit den JSON Daten.

```js
function handleXml(xml) {
      const w = $(xml).find('warehouseData');
      const warehouse = {
        warehouseID: w.find('warehouseID').text(),
        warehouseName: w.find('warehouseName').text(),
        timestamp: w.find('timestamp').text(),
        productDataList: []
      };
      w.find('ProductData').each(function() {
        const p = $(this);
        warehouse.productDataList.push({
          productID: p.find('productID').text(),
          productName: p.find('productName').text(),
          productCategory: p.find('productCategory').text(),
          productQuantity: p.find('productQuantity').text(),
          productUnit: p.find('productUnit').text()
        });
      });
      showWarehouseInfo(warehouse);
      fillTable(warehouse.productDataList);
    }
```

Diese Methode füllt die Tabelle mit XML Daten. 

# 
