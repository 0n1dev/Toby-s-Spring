package com.example.demo.user.dao;

import com.example.demo.user.domain.User;

import java.sql.Connection;
import java.sql.SQLException;

public interface UserDao {

    void add(User user) throws SQLException, ClassNotFoundException;
    User get(String id) throws SQLException, ClassNotFoundException;
    Connection getConnection() throws ClassNotFoundException, SQLException;

}
