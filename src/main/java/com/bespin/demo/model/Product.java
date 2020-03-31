package com.bespin.demo.model;

public class Product {
    private static final long serialUID = 1L;

    private String identifier;
    private String name;
    private int price;

    public Product() {
        super();
    }

    public Product(String identifier, String name, int price) {
        super();
        this.identifier = identifier;
        this.name = name;
        this.price = price;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
