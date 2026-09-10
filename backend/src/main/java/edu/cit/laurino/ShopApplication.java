package edu.cit.laurino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Parent package deliberately scans both feature modules:
 * edu.cit.laurino.shop and edu.cit.laurino.inventory.
 */
@SpringBootApplication
public class ShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopApplication.class, args);
    }
}
