package org.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArticlesTests {

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
            handle.execute("TRUNCATE TABLE articles CASCADE");
        });
    }

    @Test
    public void createArticlesFromInventoryJson() {
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
    public void creatingSameArticlesTwiceShouldReturn500() {
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
            .body(this.getClass().getClassLoader().getResourceAsStream("inventory.json"))
            .contentType("application/json")
        .when()
            .put("/articles")
        .then()
            .statusCode(500);
    }
}
