package de.jensknipper.re_director;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableCaching
@EnableTransactionManagement
@SpringBootApplication
public class ReDirectorApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReDirectorApplication.class, args);
  }
}
