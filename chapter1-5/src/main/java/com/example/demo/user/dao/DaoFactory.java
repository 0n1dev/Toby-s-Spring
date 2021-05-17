package com.example.demo.user.dao;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.FConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoFactory {

    @Bean
    public MySQLDao mySqlDao() {
        return new MySQLDao(connectionMaker());
    }

    @Bean
    public ConnectionMaker connectionMaker() {
        return new FConnection();
    }
}
