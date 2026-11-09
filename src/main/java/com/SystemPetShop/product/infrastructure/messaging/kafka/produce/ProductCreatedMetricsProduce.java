package com.SystemPetShop.product.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductCreatedMetricsProduce {

    private final OutboxStore outbox;

    public ProductCreatedMetricsProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void execute(ProductCreatedEvent event) {
        outbox.enqueue("product-created-metrics", event.idProduct().toString(), event);
    }
}
