package com.jiraclone.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
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
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource originalDataSource() {
        return DataSourceBuilder.create().build();
    }
}
