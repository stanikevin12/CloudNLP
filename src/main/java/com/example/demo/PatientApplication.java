package com.example.demo;

import com.example.demo.config.NlpCloudProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NlpCloudProperties.class)
public class PatientApplication {

        public static void main(String[] args) {
                SpringApplication.run(PatientApplication.class, args);
        }

}
