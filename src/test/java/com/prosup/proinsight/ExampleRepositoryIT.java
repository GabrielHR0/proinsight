/*
package com.prosup.proinsight;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

public class ExampleRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private MyRepository repository;

    @Test
    void shouldConnectToMongoAndPerformOps() {
        // dado
        MyEntity e = new MyEntity();
        e.setName("it-test");
        repository.save(e);

        // quando
        long count = repository.count();

        // então
        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
*/