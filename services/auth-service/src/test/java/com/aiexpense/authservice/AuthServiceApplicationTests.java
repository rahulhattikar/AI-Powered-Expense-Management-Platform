package com.aiexpense.authservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void jwtUtilsBeanExists() {
        assertThat(applicationContext.containsBean("jwtUtils")).isTrue();
    }

    @Test
    void userServiceBeanExists() {
        assertThat(applicationContext.containsBean("userService")).isTrue();
    }


    @Test
    void authControllerBeanExists() {
        assertThat(applicationContext.containsBean("authController")).isTrue();
    }

    @Test
    void securityConfigBeanExists() {
        assertThat(applicationContext.containsBean("securityConfig")).isTrue();
    }
}
