package com.bespin.demo.model;

import java.io.Serializable;

public class Product implements Serializable {

    private String id;
    private String name;
    private String category;
    private long price;
    private Long regdtm;

    public Product() {
        super();
    }

    public Product(final String id) {
        super();
        this.id = id;
    }

    public Product(String id, String name, String category, long price, Long regdtm) {
        super();
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.regdtm = regdtm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public Long getRegdtm() {
        return regdtm;
    }

    public void setRegdtm(Long regdtm) {
        this.regdtm = regdtm;
    }
}