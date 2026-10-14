package com.finsight.insights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.finsight.common", "com.finsight.insights"})
@EnableDiscoveryClient
@EnableFeignClients
public class FinancialInsightsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialInsightsApplication.class, args);
    }
}
