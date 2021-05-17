package com.example.demo.user.dao;

import com.example.demo.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback
class MySQLDaoTest {

    private ApplicationContext ac = new AnnotationConfigApplicationContext(DaoFactory.class);

    @Test
    void testConnection() throws SQLException, ClassNotFoundException {
        MySQLDao userDao = ac.getBean("mySqlDao", MySQLDao.class);

        User user = new User();

        user.setId("spring4");
        user.setName("springtest4");
        user.setPassword("password");

        userDao.add(user);

        User getUser = userDao.get(user.getId());

        assertThat(getUser.getId()).isEqualTo(user.getId());
        assertThat(getUser.getName()).isEqualTo(user.getName());
        assertThat(getUser.getPassword()).isEqualTo(user.getPassword());
    }
}