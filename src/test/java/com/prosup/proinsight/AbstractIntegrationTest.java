package com.prosup.proinsight;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Base para testes de integração.
 *
 * Como funciona:
 * - A propriedade spring.data.mongodb.uri será resolvida a partir da variável de ambiente MONGO_URI,
 *   se presente; caso contrário, usa o fallback mongodb://localhost:27017/proinsight.
 *
 * Uso:
 * - Faça seus testes de integração estenderem esta classe.
 * - No CI (GitHub Actions) já definimos MONGO_URI=mongodb://localhost:27017/proinsight,
 *   então os ITs apontarão automaticamente para o serviço Mongo provisionado.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.data.mongodb.uri=${MONGO_URI:mongodb://localhost:27017/proinsight}"
})
public abstract class AbstractIntegrationTest {

}
