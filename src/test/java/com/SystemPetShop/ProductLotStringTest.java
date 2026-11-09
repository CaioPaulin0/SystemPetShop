package com.SystemPetShop;

import com.SystemPetShop.expired.application.output.ExpiredOutput;
import com.SystemPetShop.expired.infrastructure.entity.ExpiredEntity;
import com.SystemPetShop.expired.infrastructure.http.response.ExpiredResponse;
import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import com.SystemPetShop.product.infrastructure.http.dto.ProductoDto;
import com.SystemPetShop.product.infrastructure.http.response.ProductResponse;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionEvent;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductLotStringTest {

    @ParameterizedTest
    @ValueSource(strings = {"00123", "00AB-123/26", "000"})
    void devePreservarLoteTextualNaPersistenciaEventosERespostas(String lot) {
        var date = LocalDate.of(2026, 10, 29);
        var input = new ProductoDto(UUID.randomUUID(), UUID.randomUUID(), "Racao",
                "Alimento", "Descricao", date, date.plusYears(1), lot,
                10.0, 15.0, 9, true, 1).toInput();
        var product = new Product(input.idBrand(), input.idDistributor(), input.nameProduct(),
                input.typeProduct(), input.descriptionProduct(), input.dataCreatedProduct(),
                input.dataDueProduct(), input.lotProduct(), input.priceBuy(), input.priceSell(),
                input.stockProduct(), input.dataDueProductVerify(), input.versionProduct(), true);

        var createdEntity = ProductCreatedEvent.toEntity(ProductCreatedEvent.toEvent(product));
        assertEquals(lot, createdEntity.getLotProduct());
        assertEquals(lot, ProductNewVersionEvent.toEntity(
                ProductNewVersionEvent.toEvent(product)).getLotProduct());

        try (var factory = new Configuration()
                .addAnnotatedClass(ProductEntity.class)
                .addAnnotatedClass(ExpiredEntity.class)
                .setProperty("hibernate.connection.driver_class", "org.h2.Driver")
                .setProperty("hibernate.connection.url", "jdbc:h2:mem:lot_" + UUID.randomUUID())
                .setProperty("hibernate.hbm2ddl.auto", "create-drop")
                .buildSessionFactory();
             var session = factory.openSession()) {
            var transaction = session.beginTransaction();
            session.persist(createdEntity);
            transaction.commit();
            session.clear();

            var stored = session.find(ProductEntity.class, createdEntity.getIdProduct());
            assertEquals(lot, stored.getLotProduct());
            assertEquals(lot, ProductResponse.from(ProductOutput.from(stored.toDomain())).lotProduct());
            var expired = session.find(ExpiredEntity.class, createdEntity.getIdProduct());
            assertEquals(lot, ExpiredResponse.from(ExpiredOutput.from(expired.toDomain())).lotProduct());
        }
    }
}
