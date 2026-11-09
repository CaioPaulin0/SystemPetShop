package com.SystemPetShop.brand.infrastructure.messaging.kafka.dto;

import com.SystemPetShop.brand.domain.Brand;
import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;

import java.util.UUID;

public record BrandCreatedEvent(
        UUID idBrand,
        UUID idDistributor,
        String nameBrand,
        String typeBrand
) {

    public static BrandEntity toEntity(BrandCreatedEvent event){
        return new BrandEntity(
            event.idBrand,
            event.idDistributor,
            event.nameBrand,
            event.typeBrand
        );
    }

    public static BrandCreatedEvent toEvent(Brand brand){
        return new BrandCreatedEvent(
                brand.getIdBrand().id(),
                brand.getIdDistributor(),
                brand.getNameBrand(),
                brand.getTypeBrand()
        );
    }
}
