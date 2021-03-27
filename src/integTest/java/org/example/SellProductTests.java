package org.example;

import org.example.pojos.AvailableProduct;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItems;

public class SellProductTests {
    private static Warehouse warehouse = new Warehouse();

    @BeforeAll
    public static void startWebServer() {
        warehouse.start();
    }

    @AfterAll
    public static void stopWebServer() {
        warehouse.stop();
    }

    @BeforeEach
    public void cleanUpArticlesTable() {
        warehouse.jdbi.useHandle(handle -> {
            handle.execute("TRUNCATE TABLE products_articles");
            handle.execute("TRUNCATE TABLE articles CASCADE");
            handle.execute("TRUNCATE TABLE products CASCADE");
        });
    }

    @BeforeEach
    public void insertArticlesAndProducts() {
        given()
            .port(7000)
            .body(this.getClass().getClassLoader().getResourceAsStream("inventory.json"))
            .contentType("application/json")
        .when()
            .put("/articles")
        .then()
            .statusCode(201);

        given()
            .port(7000)
            .body(this.getClass().getClassLoader().getResourceAsStream("products.json"))
            .contentType("application/json")
        .when()
            .put("/products")
        .then()
            .statusCode(201);
    }

    @Test
    public void sellInStockProduct() {
        AvailableProduct availableProduct = new AvailableProduct();
        availableProduct.setName("Dinning Table");
        availableProduct.setQuantity(1);

        given()
                .port(7000)
                .body(availableProduct)
                .contentType("application/json")
                .when()
                .delete("/products")
                .then()
                .statusCode(200);

        // given the test data, selling the table should leave us with enough articles to create one chair
        given()
            .port(7000)
        .when()
            .get("/products")
        .then()
            .body("name", hasItems("Dining Chair"))
            .body("quantity", hasItems(1))
            .body("price", hasItems(700))
            .statusCode(200);

    }

    @Test
    public void sellOutOfStockProduct() {
        AvailableProduct availableProduct = new AvailableProduct();
        availableProduct.setName("Dinning Table");
        availableProduct.setQuantity(5);

        given()
            .port(7000)
            .body(availableProduct)
            .contentType("application/json")
        .when()
            .delete("/products")
        .then()
            .statusCode(500);
    }
}
