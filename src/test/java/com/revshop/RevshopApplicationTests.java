package com.revshop;

import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.Assert.assertNotNull;

public class RevshopApplicationTests {

    @Test
    public void revshopApplication_isAnnotatedForBootstrapping() {
        assertNotNull(RevshopApplication.class.getAnnotation(SpringBootApplication.class));
    }
}
