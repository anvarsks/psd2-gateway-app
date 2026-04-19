package com.example.adapterdnb;

import com.example.adapterdnb.config.MockDnbBankProperties;
import com.example.adapterdnb.security.InternalAccessPolicy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({InternalAccessPolicy.class, MockDnbBankProperties.class})
public class AdapterDnbApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdapterDnbApplication.class, args);
    }
}
