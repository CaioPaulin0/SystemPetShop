package com.SystemPetShop.brand.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Brand {
    public IdBrand idBrand;
    public UUID idDistributor;
    private String nameBrand;
    private String typeBrand;

    public Brand(UUID idDistributor, String nameBrand, String typeBrand) {
        this.idBrand = new IdBrand();
        this.idDistributor = idDistributor;
        this.nameBrand = nameBrand;
        this.typeBrand = typeBrand;
    }
}
