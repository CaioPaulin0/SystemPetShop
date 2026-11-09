package com.SystemPetShop.product.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record ProductDeleteEvent(UUID idProduct) {
    public static ProductDeleteEvent toEvent(UUID id){
        return new ProductDeleteEvent(id);
    }
}
