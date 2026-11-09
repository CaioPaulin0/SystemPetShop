package com.SystemPetShop.brand.infrastructure.http.dto;

import com.SystemPetShop.brand.application.input.BrandEditInput;

import java.util.UUID;

public record BrandEditDto(
        UUID idBrand,
        UUID idDistributor,
        String nameBrand,
        String typeBrand
) {

    public static BrandEditInput toInput(BrandEditDto brandEditDto){
        return new BrandEditInput(
                brandEditDto.idBrand(),
                brandEditDto.idDistributor(),
                brandEditDto.nameBrand(),
                brandEditDto.typeBrand()
        );
    }
}
