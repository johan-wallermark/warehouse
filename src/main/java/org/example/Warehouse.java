package org.example;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.OpenApiPluginConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
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
                .create(config -> {
                    var deprecatedDocsPath = "/api/openapi.json";
                    config.plugins.register(new OpenApiPlugin(getOpenApiConfiguration(deprecatedDocsPath)));

                    config.plugins.register(new SwaggerPlugin(getSwaggerConfiguration(deprecatedDocsPath)));
                })
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

    private SwaggerConfiguration getSwaggerConfiguration(String deprecatedDocsPath) {
        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration();
        swaggerConfiguration.setDocumentationPath(deprecatedDocsPath);
        return swaggerConfiguration;
    }

    private OpenApiPluginConfiguration getOpenApiConfiguration(String documentationPath) {
        return new OpenApiPluginConfiguration()
                .withDocumentationPath(documentationPath)
                .withDefinitionConfiguration((version, definition) -> definition
                        .withOpenApiInfo((openApiInfo) -> {
                            openApiInfo.setTitle("Demo app - Documentation");
                            openApiInfo.setVersion("1.0.0");
                            openApiInfo.setDescription("Demo warehouse application that has inventory consisting of Articles take make up Products.");
                        })
                        .withServer((openApiServer) -> {
                            openApiServer.setUrl(("http://localhost:{port}{basePath}/" + version + "/"));
                            openApiServer.setDescription("Server description goes here");
                            openApiServer.addVariable("port", "7000", new String[]{"7000", "8080"}, "Port of the server");
                            openApiServer.addVariable("basePath", "", new String[]{"", "v1"}, "Base path of the server");
                        })
                )
                ;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void stop() {
        logger.info("Shutting down application server and database connection pool");
        app.stop();
        dataSource.getHikariDataSource().close();
        logger.info("Done shutting down. Goodbye...");
    }
}
