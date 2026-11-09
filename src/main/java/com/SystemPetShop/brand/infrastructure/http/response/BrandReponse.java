package com.SystemPetShop.brand.infrastructure.http.response;

import com.SystemPetShop.brand.application.output.BrandOutput;

import java.util.UUID;

public record BrandReponse(
        UUID idBrand,
        UUID idDistributor,
        String nameBrand,
        String typeBrand
) {
    public static BrandReponse from(BrandOutput brandOutput){
        return new BrandReponse(
                brandOutput.idBrand(),
                brandOutput.idDistributor(),
                brandOutput.nameBrand(),
                brandOutput.typeBrand()
        );
    }
}
