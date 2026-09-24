package edu.cit.laurino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Parent package deliberately scans all feature modules:
 * edu.cit.laurino.shop, edu.cit.laurino.inventory, edu.cit.laurino.notification,
 * and edu.cit.laurino.supplier. @EnableScheduling powers the supplier
 * module's pending-retry and delivery-tracking jobs.
 */
@SpringBootApplication
@EnableScheduling
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
