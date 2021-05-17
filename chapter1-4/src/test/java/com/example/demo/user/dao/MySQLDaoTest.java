package com.example.demo.user.dao;

import com.example.demo.connection.FConnection;
import com.example.demo.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MySQLDaoTest {

    @Test
    void testConnection() throws SQLException, ClassNotFoundException {
        final MySQLDao userDao = new DaoFactory().mySqlDao();

        User user = new User();

        user.setId("spring3");
        user.setName("springtest3");
        user.setPassword("password");

        userDao.add(user);

        User getUser = userDao.get(user.getId());

        assertThat(getUser.getId()).isEqualTo(user.getId());
        assertThat(getUser.getName()).isEqualTo(user.getName());
        assertThat(getUser.getPassword()).isEqualTo(user.getPassword());
    }
}