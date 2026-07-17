package com.github.nicolasholanda.shardedledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    // Data source properties
    @Bean
    @ConfigurationProperties(prefix = "app.datasource.shard0")
    public DataSourceProperties shard0Properties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.shard1")
    public DataSourceProperties shard1Properties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.shard2")
    public DataSourceProperties shard2Properties() {
        return new DataSourceProperties();
    }

    // Data sources
    @Bean
    public DataSource shard0DataSource() {
        return shard0Properties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource shard1DataSource() {
        return shard1Properties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource shard2DataSource() {
        return shard2Properties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource routingDataSource() {
        ShardRoutingDataSource routingDataSource = new ShardRoutingDataSource();
        routingDataSource.setTargetDataSources(Map.of(
                "shard0", shard0DataSource(),
                "shard1", shard1DataSource(),
                "shard2", shard2DataSource()
        ));
        routingDataSource.setDefaultTargetDataSource(shard0DataSource());
        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource() {
        // LazyConnectionDataSourceProxy connects to the database when needed
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
