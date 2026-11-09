package com.SystemPetShop.brand.application.output;

import com.SystemPetShop.brand.domain.Brand;
import com.SystemPetShop.brand.domain.IdBrand;

import java.util.UUID;

public record BrandOutput(
        UUID idBrand,
        UUID idDistributor,
        String nameBrand,
        String typeBrand
) {

    public static BrandOutput from(Brand brand){
        return new BrandOutput(
                brand.idBrand.id(),
                brand.idDistributor,
                brand.getNameBrand(),
                brand.getTypeBrand()
        );
    }
}
