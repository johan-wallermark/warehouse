package org.example;

import io.javalin.Javalin;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.controllers.ArticleController;
import org.example.controllers.ProductController;
import org.example.db.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.postgres.PostgresPlugin;

public class Warehouse {
    private static final Logger logger = LogManager.getLogger();
    private Javalin app;
    private DataSource dataSource;
    Jdbi jdbi;

    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        warehouse.start();
        warehouse.addShutdownHook();
    }

    public void start() {
        dataSource = new DataSource();
        jdbi = Jdbi
                .create(dataSource.getHikariDataSource())
                .installPlugin(new PostgresPlugin());

        app = Javalin
                .create(config -> config.registerPlugin(new OpenApiPlugin(getOpenApiOptions())))
                .start(7000);

        var articleEndpoint = new ArticleController(jdbi);
        var productEndpoint = new ProductController(jdbi);

        app
                .put("articles", articleEndpoint::create)
                .get("products", productEndpoint::getAllProducts)
                .delete("products", productEndpoint::sellProduct)
                .put("products", productEndpoint::create)
                .exception(UnableToExecuteStatementException.class, (e, ctx) -> {
                    ctx.status(500);
                    ctx.result("Failed to execute the statement with the current input. " + e.getMessage());
                });

        logger.info("Application is now running.");
    }

    private static OpenApiOptions getOpenApiOptions() {
        Info applicationInfo = new Info()
                .version("0.1.1")
                .description("Demo warehouse application that has inventory consisting of Articles take make up Products.");
        return new OpenApiOptions(applicationInfo)
                .path("/swagger-docs")
                .activateAnnotationScanningFor("org.example")
                .swagger(new SwaggerOptions("/").title("Swagger Documentation"));
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stop();
        }));
    }

    public void stop() {
        logger.info("Shutting down application server and database connection pool");
        app.stop();
        dataSource.getHikariDataSource().close();
        logger.info("Done shutting down. Goodbye...");
    }
}
