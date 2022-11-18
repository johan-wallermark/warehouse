package org.example.controllers;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pojos.ArticleContainer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

public class ArticleController {
    private static final Logger logger = LogManager.getLogger();
    private final Jdbi jdbi;

    public ArticleController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @OpenApi(
            path = "/articles",
            methods = HttpMethod.PUT,
            description = "Adds articles to the database.",
            requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ArticleContainer.class)),
            responses = {
                    @OpenApiResponse(status = "201"),
                    @OpenApiResponse(status = "500")
            }
    )
    public void create(Context context) {
        var inventory = context.bodyAsClass(ArticleContainer.class);

        jdbi.useHandle(handle -> {
            PreparedBatch batch = handle.prepareBatch("INSERT INTO articles(id, name, stock) VALUES(:id, :name, :stock)");
            for (var article : inventory.getArticles()) {
                logger.debug("Adding article: {}", article);
                batch.bindBean(article).add();
            }
            batch.execute();
        });
        context.status(201);
    }
}
