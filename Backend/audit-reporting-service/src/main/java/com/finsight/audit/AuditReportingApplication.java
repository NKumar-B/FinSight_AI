package com.finsight.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.finsight.common", "com.finsight.audit"})
@EnableDiscoveryClient
@EnableFeignClients
public class AuditReportingApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditReportingApplication.class, args);
    }
}
