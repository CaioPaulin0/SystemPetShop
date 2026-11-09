package com.SystemPetShop.brand.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record BrandDeleteEvent(UUID idBrand) {
    public static BrandDeleteEvent toEvent(UUID id){
        return new BrandDeleteEvent(id);
    }

}
