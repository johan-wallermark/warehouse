package org.example.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Properties;

public class DataSource {

    private final HikariDataSource hikariDataSource;

    public DataSource() {
        var props = new Properties();
        props.setProperty("dataSourceClassName",
                System.getenv("DATA_SOURCE_CLASS_NAME") != null ? System.getenv("DATA_SOURCE_CLASS_NAME") : "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.portNumber",
                System.getenv("DATA_SOURCE_PORT_NUMBER") != null ? System.getenv("DATA_SOURCE_PORT_NUMBER") : "32001");
        props.setProperty("dataSource.serverName",
                System.getenv("DATA_SOURCE_SERVER_NAME") != null ? System.getenv("DATA_SOURCE_SERVER_NAME") : "localhost");
        props.setProperty("dataSource.user",
                System.getenv("DATA_SOURCE_USER") != null ? System.getenv("DATA_SOURCE_USER") : "postgres");
        props.setProperty("dataSource.password",
                System.getenv("DATA_SOURCE_PASSWORD") != null ? System.getenv("DATA_SOURCE_PASSWORD") : "secretpassword");
        props.setProperty("dataSource.databaseName",
                System.getenv("DATA_SOURCE_DATABASE_NAME") != null ? System.getenv("DATA_SOURCE_DATABASE_NAME") : "warehouse");

        var config = new HikariConfig(props);
        hikariDataSource = new HikariDataSource(config);
    }

    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }
}
