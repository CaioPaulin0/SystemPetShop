package com.SystemPetShop.brand.infrastructure.http.dto;

import com.SystemPetShop.brand.application.input.BrandInput;

import java.util.UUID;

public record BrandDto(
        UUID idDistributor,
        String nameBrand,
        String typeBrand
) {

    public BrandInput toInput(){
        return new BrandInput(
                idDistributor,
                nameBrand,
                typeBrand
        );
    }
}
