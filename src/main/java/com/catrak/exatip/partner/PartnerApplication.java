package com.catrak.exatip.partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableEurekaClient
@SpringBootApplication
@EnableSwagger2
@EntityScan(basePackages = { "com.catrak.exatip.partner", "com.catrak.exatip.commonlib" })
@ComponentScan(basePackages = { "com.catrak.exatip.partner", "com.catrak.exatip.commonlib" })
@EnableJpaRepositories(basePackages = { "com.catrak.exatip.partner", "com.catrak.exatip.commonlib" })
public class PartnerApplication {
    public static void main(String[] args) {
        SpringApplication.run(PartnerApplication.class, args);
    }
}