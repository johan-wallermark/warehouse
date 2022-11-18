package org.example.controllers;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pojos.AvailableProduct;
import org.example.pojos.Product;
import org.example.pojos.ProductContainer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;

import java.util.List;

public class ProductController {
    private static final Logger logger = LogManager.getLogger();
    private final Jdbi jdbi;

    public ProductController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @OpenApi(
            path = "/products",
            methods = HttpMethod.PUT,
            description = "Adds products to the database. Note that articles that the product is comprised of needs to be created first.",
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Product.class)),
            responses = {
                    @OpenApiResponse(status = "201"),
                    @OpenApiResponse(status = "500")
            }
    )
    public void create(Context context) {
        var productContainer = context.bodyAsClass(ProductContainer.class);
        logger.debug("Adding products {}", productContainer);

        jdbi.useTransaction(handle -> {
            // Add products and get IDs since they weren't part of the products.json example
            var productsBatch = handle.prepareBatch("INSERT INTO products(name, price) VALUES(:name, :price)");
            for (var product : productContainer.getProducts()) {
                logger.trace("Adding product: {}", product);
                productsBatch.bindBean(product).add();
            }
            ResultIterable<Product> result = productsBatch.executeAndReturnGeneratedKeys().mapToBean(Product.class);

            var productsArticlesBatch = handle.prepareBatch("INSERT INTO products_articles(art_id, prod_id, art_quantity) VALUES(:artId, :prodId, :artQuantity)");
            for (Product generatedProduct : result) {
                logger.trace("Product with generated key {}", generatedProduct);
                var product = productContainer.getProducts().stream()
                        .filter(p -> p.getName().equals(generatedProduct.getName()))
                        .findAny()
                        .orElseThrow();
                for (var productsArticles : product.getProductsArticles()) {
                    logger.trace("Setting prod id for {} to {}", productsArticles, generatedProduct.getId());
                    productsArticles.setProdId(generatedProduct.getId());
                    productsArticlesBatch.bindBean(productsArticles).add();
                }
            }
            productsArticlesBatch.execute();
        });
        context.status(201);
    }

    @OpenApi(
            path = "/products",
            methods = HttpMethod.GET,
            description = "Returns all products that are available for sale based on the available stock.",
            responses = {
                    @OpenApiResponse(status = "201", content = @OpenApiContent(from = AvailableProduct[].class)),
                    @OpenApiResponse(status = "500")
            }
    )
    public void getAllProducts(Context context) {
        List<AvailableProduct> availableInventories = jdbi.withHandle(handle -> {
            String query = """
                    select name, price, min(quantity) as quantity
                    from (
                        select
                            p.name, p.price, a.stock / pa.art_quantity as quantity
                        from
                            products as p
                        left join products_articles as pa
                            on p.id = pa.prod_id
                        left join articles as a
                            on pa.art_id = a.id
                    ) as joined_tables
                    group by name, price
                    having min(quantity) > 0
                    """;
            return handle.createQuery(query)
                    .mapToBean(AvailableProduct.class)
                    .list();
        });
        context.json(availableInventories);
    }

    @OpenApi(
            path = "/products",
            methods = HttpMethod.DELETE,
            description = "Sells the product reducing the available stock for the articles that make up the product.",
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = AvailableProduct.class)),
            responses = {
                    @OpenApiResponse(status = "200"),
                    @OpenApiResponse(status = "500", content = @OpenApiContent(from = String.class))
            }
    )
    public void sellProduct(Context context) {
        var availableProduct = context.bodyAsClass(AvailableProduct.class);
        jdbi.inTransaction(handle -> handle
                .createUpdate("""
                        UPDATE
                            articles a
                        SET
                            stock = stock - (:quantity * art_quantity)
                        FROM (
                            SELECT art_id, art_quantity
                            FROM products p
                            LEFT JOIN products_articles pa
                            ON p.id = pa.prod_id
                            WHERE p.name = :name
                        ) joined_table
                        WHERE a.id = joined_table.art_id
                        """)
                .bindBean(availableProduct)
                .execute());
    }
}
