package com.jiraclone.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Qualifier("originalDataSource") DataSource originalDataSource
    ) {
        return new TenantAwareDataSourceWrapper(originalDataSource);
    }

    @Bean(name = "originalDataSource")
    public DataSource originalDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
