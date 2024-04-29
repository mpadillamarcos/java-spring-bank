package mpadillamarcos.javaspringbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class JavaSpringBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaSpringBankApplication.class, args);
    }

}
