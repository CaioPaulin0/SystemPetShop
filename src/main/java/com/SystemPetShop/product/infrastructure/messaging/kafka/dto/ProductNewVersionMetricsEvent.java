package com.SystemPetShop.product.infrastructure.messaging.kafka.dto;

import com.SystemPetShop.product.domain.Product;

import java.util.UUID;

public record ProductNewVersionMetricsEvent(
        UUID previousProductId,
        ProductNewVersionEvent product
) {
    public static ProductNewVersionMetricsEvent toEvent(UUID previousProductId, Product product) {
        return new ProductNewVersionMetricsEvent(previousProductId, ProductNewVersionEvent.toEvent(product));
    }
}
