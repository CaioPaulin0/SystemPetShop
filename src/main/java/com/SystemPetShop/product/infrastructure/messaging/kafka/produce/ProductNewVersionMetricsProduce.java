package com.SystemPetShop.product.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionMetricsEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductNewVersionMetricsProduce {

    private final OutboxStore outbox;

    public ProductNewVersionMetricsProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void execute(ProductNewVersionMetricsEvent event) {
        outbox.enqueue("product-new-version-metrics", event.product().idProduct().toString(), event);
    }
}
