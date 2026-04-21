package com.example.sellsumm;

public class ProductModel
{

    private String productId;
    private double price;
    private String productName;
    private String productCategory;
    private String productType; // "Default" or "Add-on"

    public ProductModel() {} // Required for Firestore

    public ProductModel(String productId, double price, String productName,String productCategory, String productType)
    {

        this.productId   = productId;
        this.price       = price;
        this.productName = productName;
        this.productCategory = productCategory;
        this.productType = productType;
    }

    // Getters
    public String getProductId()   { return productId; }
    public double getPrice()       { return price; }
    public String getProductName() { return productName; }
    public String getProductCategory() {return productCategory;}
    public String getProductType() { return productType; }

    // Setters
    public void setProductId(String productId)     { this.productId = productId; }
    public void setPrice(double price)             { this.price = price; }
    public void setProductName(String productName) { this.productName = productName; }

    public void setProductCategory(String productCategory) {this.productCategory = productCategory;}

    public void setProductType(String productType) { this.productType = productType; }
}