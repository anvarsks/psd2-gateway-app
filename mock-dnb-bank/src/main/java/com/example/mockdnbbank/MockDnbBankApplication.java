package com.example.mockdnbbank;

import com.example.mockdnbbank.security.InternalAccessPolicy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InternalAccessPolicy.class)
public class MockDnbBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockDnbBankApplication.class, args);
    }
}
