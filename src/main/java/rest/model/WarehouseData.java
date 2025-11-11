package rest.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
@JacksonXmlRootElement(localName = "warehouseData")
public class WarehouseData {
	
	private String warehouseID;
	private String warehouseName;
	private String timestamp;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "ProductData")
	private ArrayList<ProductData> productDataList;

	public WarehouseData() {
		
		this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());



	}
	
	/**
	 * Setter and Getter Methods
	 */
	public String getWarehouseID() {
		return warehouseID;
	}

	public void setWarehouseID(String warehouseID) {
		this.warehouseID = warehouseID;
	}

	public String getWarehouseName() {
		return warehouseName;
	}

	public void setWarehouseName(String warehouseName) {
		this.warehouseName = warehouseName;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Methods
	 */
	@Override
	public String toString() {
		String info = String.format("Warehouse Info: ID = %s, timestamp = %s", warehouseID, timestamp );
		return info;
	}

    public ArrayList<ProductData> getProductDataList() {
        return productDataList;
    }

    public void setProductDataList(ArrayList<ProductData> productDataList) {
        this.productDataList = productDataList;
    }
}
