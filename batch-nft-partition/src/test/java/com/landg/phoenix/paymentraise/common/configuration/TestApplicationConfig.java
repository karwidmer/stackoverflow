package com.landg.phoenix.paymentraise.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class TestApplicationConfig {

    @Value("jdbc:postgresql")
    private String url;

    @Value("postgres")
    private String username;

    @Value("x7Gt8Blg7")
    private String password;

    @Value("org.postgresql.Driver")
    private String driverClassName;

    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(this.driverClassName);
        dataSource.setUrl(this.url);
        dataSource.setUsername(this.username);
        dataSource.setPassword(this.password);
        return dataSource;
    }

}
