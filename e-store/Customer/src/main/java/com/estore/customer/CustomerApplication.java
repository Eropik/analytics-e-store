package com.estore.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.estore.library","com.estore.customer"})
@EnableJpaRepositories(value= "com.estore.library.repository")
@EntityScan(value="com.estore.library.model")
public class CustomerApplication {

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));
        SpringApplication.run(CustomerApplication.class, args);
    }

}
