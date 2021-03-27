package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;

public class GetAvailableProductTests {
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
    public void getAvailableInventory() {
        given()
            .port(7000)
        .when()
            .get("/products")
        .then()
            .body("name", hasItems("Dining Chair", "Dinning Table"))
            .body("quantity", hasItems(1, 2))
            .body("price", hasItems(0, 700))
            .statusCode(200);
    }
}
