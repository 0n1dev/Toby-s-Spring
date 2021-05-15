package com.example.demo.user.dao;

import com.example.demo.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private static final UserDao userDao = new UserDao();

    @Test
    @DisplayName("add후 get을하면 결과가 동일하게 노출")
    void test() throws SQLException, ClassNotFoundException {
        User user = new User();

        user.setId("spring");
        user.setName("springtest");
        user.setPassword("password");

        userDao.add(user);

        User getUser = userDao.get(user.getId());

        assertThat(getUser.getId()).isEqualTo(user.getId());
        assertThat(getUser.getName()).isEqualTo(user.getName());
        assertThat(getUser.getPassword()).isEqualTo(user.getPassword());
    }
}