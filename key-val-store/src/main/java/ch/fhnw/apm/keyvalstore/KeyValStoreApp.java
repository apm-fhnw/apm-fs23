package ch.fhnw.apm.keyvalstore;

import ch.fhnw.apm.keyvalstore.storage.LocalStorage;
import ch.fhnw.apm.keyvalstore.storage.Storage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KeyValStoreApp {

    public static void main(String[] args) {
        SpringApplication.run(KeyValStoreApp.class, args);
    }

    @Bean
    Storage storage() {
        return new LocalStorage();
    }
}
