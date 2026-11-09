package com.SystemPetShop.product.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductNewVersionProduce {
    private final OutboxStore outbox;

    public ProductNewVersionProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void execute(ProductNewVersionEvent event){
        outbox.enqueue(
                "product-new-version",
                event.idProduct().toString(),
                event
        );
    }
}
