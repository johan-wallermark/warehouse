package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreateProductsTests {

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
    public void insertArticles() {
        given()
            .port(7000)
            .body(this.getClass().getClassLoader().getResourceAsStream("inventory.json"))
            .contentType("application/json")
        .when()
            .put("/articles")
        .then()
            .statusCode(201);
    }

    @Test
    public void createProductsFromProductsJson() {
        given()
            .port(7000)
            .body(this.getClass().getClassLoader().getResourceAsStream("products.json"))
            .contentType("application/json")
        .when()
            .put("/products")
        .then()
            .statusCode(201);

        warehouse.jdbi.useHandle(handle -> {
            Integer itemsInProductsTable = handle.createQuery("select count(*) from products").mapTo(Integer.class).one();
            assertThat("Number of items in the products table", itemsInProductsTable, equalTo(2));

            Integer itemsInProductsArticlesTable = handle.createQuery("select count(*) from products_articles").mapTo(Integer.class).one();
            assertThat("Number of items in the products_articles table", itemsInProductsArticlesTable, equalTo(6));
        });
    }

    @Test
    public void creatingSameProductsTwiceShouldReturn500() {
        given()
            .port(7000)
            .body(this.getClass().getClassLoader().getResourceAsStream("products.json"))
            .contentType("application/json")
        .when()
            .put("/products")
        .then()
            .statusCode(201);

        given()
            .port(7000)
            .body(this.getClass().getClassLoader().getResourceAsStream("products.json"))
            .contentType("application/json")
        .when()
            .put("/products")
        .then()
            .statusCode(500);
    }
}
