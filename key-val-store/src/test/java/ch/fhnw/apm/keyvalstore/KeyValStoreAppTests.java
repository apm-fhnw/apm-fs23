package ch.fhnw.apm.keyvalstore;

import ch.fhnw.apm.keyvalstore.storage.Storage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KeyValStoreAppTests {

    @Autowired
    private Storage storage;

    @Test
    void contextLoads() {
        assertThat(storage).isNotNull();
    }
}
