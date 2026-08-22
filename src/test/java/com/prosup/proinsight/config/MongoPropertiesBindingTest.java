package com.prosup.proinsight.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "spring.data.mongodb.host=localhost",
        "spring.data.mongodb.port=27017",
        "spring.data.mongodb.database=testdb"
})
@ActiveProfiles("test")
class MongoPropertiesBindingTest {

    @Autowired
    private MongoProperties mongoProperties;

    @Test
    void shouldBindProperties() {
        assertEquals("localhost", mongoProperties.getHost());
        assertEquals(27017, mongoProperties.getPort());
        assertEquals("testdb", mongoProperties.getDatabase());
    }
}