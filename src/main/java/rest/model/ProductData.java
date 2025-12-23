package rest.model;

public class ProductData {
    private String productID;
    private String productName;
    private String productCategory;
    private int productQuantity;
    private String productUnit;

    public ProductData() {
    }

    public ProductData(String id, String name, String category, int quantity, String unit) {
        this.productID = id;
        this.productName = name;
        this.productCategory = category;
        this.productQuantity = quantity;
        this.productUnit = unit;

    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductUnit() {
        return productUnit;
    }

    public void setProductUnit(String productUnit) {
        this.productUnit = productUnit;
    }
}
