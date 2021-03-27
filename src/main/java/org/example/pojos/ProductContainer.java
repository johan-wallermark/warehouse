package org.example.pojos;

import java.util.List;

public class ProductContainer {
    List<Product> products;

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "ProductContainer{" +
                "products=" + products +
                '}';
    }
}
