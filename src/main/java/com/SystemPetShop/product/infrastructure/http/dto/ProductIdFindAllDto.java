package com.SystemPetShop.product.infrastructure.http.dto;

import com.SystemPetShop.product.application.input.ProductInputList;

import java.util.UUID;

public record ProductIdFindAllDto(
        UUID idBrand
) {

    public ProductInputList toInput(){
        return new ProductInputList(idBrand);
    }
}
