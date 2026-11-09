package com.SystemPetShop.product.infrastructure.messaging.kafka.dto;

import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;

import java.time.LocalDate;
import java.util.UUID;

public record ProductNewVersionEvent(
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
    public static ProductNewVersionEvent toEvent(Product product){
        return new ProductNewVersionEvent(
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

    public static ProductEntity toEntity(ProductNewVersionEvent event){
        return new ProductEntity(
                event.idProduct(),
                event.idBrand(),
                event.idDistributor(),
                event.nameProduct(),
                event.typeProduct(),
                event.descriptionProduct(),
                event.dataCreated(),
                event.dataCreatedProduct(),
                event.dataDueProduct(),
                event.lotProduct(),
                event.priceBuy(),
                event.priceSell(),
                event.stockProduct(),
                event.dataDueProductVerify(),
                event.versionProduct(),
                event.productEnableDue()
        );
    }

    public static ProductEntity eventToEntity(ProductEntity event){
        return new ProductEntity(
                event.getIdProduct(),
                event.getIdBrand(),
                event.getIdDistributor(),
                event.getNameProduct(),
                event.getTypeProduct(),
                event.getDescriptionProduct(),
                event.getDataCreated(),
                event.getDataCreatedProduct(),
                event.getDataDueProduct(),
                event.getLotProduct(),
                event.getPriceBuy(),
                event.getPriceSell(),
                event.getStockProduct(),
                event.isDataDueProductVerify(),
                event.getVersionProduct(),
                event.isProductEnableDue()
        );
    }
}
