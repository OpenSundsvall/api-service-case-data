package se.sundsvall.casedata;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import se.sundsvall.dept44.ServiceApplication;

@ServiceApplication
@EnableFeignClients
public class CaseDataApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseDataApplication.class, args);
    }

}
