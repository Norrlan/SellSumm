package com.example.sellsumm;

public class ProductModel {

    private String productId;
    private String sku;
    private double price;
    private String productName;
    private String productType; // "Default" or "Add-on"

    public ProductModel() {} // Required for Firestore

    public ProductModel(String productId, String sku, double price,
                        String productName, String productType) {
        this.productId   = productId;
        this.sku         = sku;
        this.price       = price;
        this.productName = productName;
        this.productType = productType;
    }

    // Getters
    public String getProductId()   { return productId; }
    public String getSku()         { return sku; }
    public double getPrice()       { return price; }
    public String getProductName() { return productName; }
    public String getProductType() { return productType; }

    // Setters
    public void setProductId(String productId)     { this.productId = productId; }
    public void setSku(String sku)                 { this.sku = sku; }
    public void setPrice(double price)             { this.price = price; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductType(String productType) { this.productType = productType; }
}