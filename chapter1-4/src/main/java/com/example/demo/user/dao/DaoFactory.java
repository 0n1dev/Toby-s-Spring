package com.example.demo.user.dao;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.FConnection;

public class DaoFactory {

    public MySQLDao mySqlDao() {
        return new MySQLDao(connectionMaker());
    }

    public ConnectionMaker connectionMaker() {
        return new FConnection();
    }
}
