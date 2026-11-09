package com.SystemPetShop.product.infrastructure.messaging.kafka.dto;

import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;

import java.time.LocalDate;
import java.util.UUID;

public record ProductCreatedEvent(
        UUID idProduct,
        UUID idBrand,
        UUID idDistributor,
        String nameProduct,
        String typeProduct,
        String descriptionProduct,
        LocalDate dataCreated,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        String lotProduct,
        double priceBuy,
        double priceSell,
        long stockProduct,
        boolean dataDueProductVerify,
        long versionProduct,
        boolean productEnableDue
) {
    public static ProductEntity toEntity(ProductCreatedEvent event){
        return new ProductEntity(
                event.idProduct,
                event.idBrand,
                event.idDistributor,
                event.nameProduct,
                event.typeProduct,
                event.descriptionProduct,
                event.dataCreated,
                event.dataCreatedProduct,
                event.dataDueProduct,
                event.lotProduct,
                event.priceBuy,
                event.priceSell,
                event.stockProduct,
                event.dataDueProductVerify,
                event.versionProduct,
                event.productEnableDue
        );
    }

    public static ProductCreatedEvent toEvent(Product product){
        return new ProductCreatedEvent(
                product.getIdProduct().id(),
                product.getIdBrand(),
                product.getIdDistributor(),
                product.getNameProduct(),
                product.getTypeProduct(),
                product.getDescriptionProduct(),
                product.getDataCreated(),
                product.getDataCreatedProduct(),
                product.getDataDueProduct(),
                product.getLotProduct(),
                product.getPriceBuy(),
                product.getPriceSell(),
                product.getStockProduct(),
                product.isDataDueProductVerify(),
                product.getVersionProduct(),
                product.isProductEnableDue()
        );
    }

    public static ProductCreatedEvent entityToEvent(ProductEntity product){
        return new ProductCreatedEvent(
                product.getIdProduct(),
                product.getIdBrand(),
                product.getIdDistributor(),
                product.getNameProduct(),
                product.getTypeProduct(),
                product.getDescriptionProduct(),
                product.getDataCreated(),
                product.getDataCreatedProduct(),
                product.getDataDueProduct(),
                product.getLotProduct(),
                product.getPriceBuy(),
                product.getPriceSell(),
                product.getStockProduct(),
                product.isDataDueProductVerify(),
                product.getVersionProduct(),
                product.isProductEnableDue()
        );
    }
}
