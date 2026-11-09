package com.SystemPetShop.product.infrastructure.http.dto;

import com.SystemPetShop.product.application.input.ProductInput;

import java.time.LocalDate;
import java.util.UUID;

public record ProductoDto(
        UUID idBrand,
        UUID idDistributor,
        String nameProduct,
        String typeProduct,
        String descriptionProduct,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        String lotProduct,
        double priceBuy,
        double priceSell,
        long stockProduct,
        boolean dataDueProductVerify,
        long versionProduct
) {
    public ProductInput toInput(){
        return new ProductInput(
                idBrand,
                idDistributor,
                nameProduct,
                typeProduct,
                descriptionProduct,
                dataCreatedProduct,
                dataDueProduct,
                lotProduct,
                priceBuy,
                priceSell,
                stockProduct,
                dataDueProductVerify,
                versionProduct
        );
    }
}
