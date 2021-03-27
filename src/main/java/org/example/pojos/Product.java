package org.example.pojos;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public class Product {
    private int id;
    private String name;
    private int price;

    @JsonAlias("contain_articles")
    private List<ProductArticle> productsArticles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public List<ProductArticle> getProductsArticles() {
        return productsArticles;
    }

    public void setProductsArticles(List<ProductArticle> productsArticles) {
        this.productsArticles = productsArticles;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", productsArticles=" + productsArticles +
                '}';
    }
}
