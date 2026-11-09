package com.SystemPetShop.product.infrastructure.http.dto;

import com.SystemPetShop.product.application.input.ProductEditInput;

import java.time.LocalDate;
import java.util.UUID;

public record ProductEditDto(
        UUID idProduct,
        UUID idBrand,
        UUID idDistributor,
        String typeProduct,
        String descriptionProduct,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        String lotProduct,
        double priceBuy,
        double priceSell,
        long stockProduct
) {

    public ProductEditInput toInput(ProductEditDto productEditDto){
        return new ProductEditInput(
                productEditDto.idProduct,
                productEditDto.idBrand,
                productEditDto.idDistributor,
                productEditDto.typeProduct,
                productEditDto.descriptionProduct,
                productEditDto.dataCreatedProduct,
                productEditDto.dataDueProduct,
                productEditDto.lotProduct,
                productEditDto.priceBuy,
                productEditDto.priceSell,
                productEditDto.stockProduct

        );
    }
}
