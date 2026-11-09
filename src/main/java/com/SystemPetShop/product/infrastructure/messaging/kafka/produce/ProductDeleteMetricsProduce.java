package com.SystemPetShop.product.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductDeleteMetricsProduce {

    private final OutboxStore outbox;

    public ProductDeleteMetricsProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void execute(ProductDeleteEvent event) {
        outbox.enqueue("product-delete-metrics", event.idProduct().toString(), event);
    }
}
