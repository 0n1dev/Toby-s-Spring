package com.example.demo.user.dao;

import com.example.demo.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    @Test
    @DisplayName("MySQL 테스트")
    void testMySQL() throws SQLException, ClassNotFoundException {
        final UserDao userDao = new MySQLDao();

        User user = new User();

        user.setId("spring2");
        user.setName("springtest2");
        user.setPassword("password");

        userDao.add(user);

        User getUser = userDao.get(user.getId());

        assertThat(getUser.getId()).isEqualTo(user.getId());
        assertThat(getUser.getName()).isEqualTo(user.getName());
        assertThat(getUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @DisplayName("PostGreSQL 테스트")
    void testPostGreSQL() throws SQLException, ClassNotFoundException {
        final UserDao userDao = new PostGreSQLDao();

        User user = new User();

        user.setId("spring2");
        user.setName("springtest2");
        user.setPassword("password");

        userDao.add(user);

        User getUser = userDao.get(user.getId());

        // 공백이 왜들어갈까???????????
        assertThat(getUser.getId().trim()).isEqualTo(user.getId());
        assertThat(getUser.getName().trim()).isEqualTo(user.getName());
        assertThat(getUser.getPassword().trim()).isEqualTo(user.getPassword());
    }
}