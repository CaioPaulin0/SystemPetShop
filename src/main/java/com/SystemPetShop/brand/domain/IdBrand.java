package com.SystemPetShop.brand.domain;

import java.util.UUID;

public record IdBrand(UUID id) {
    public IdBrand(){
        this(UUID.randomUUID());
    }

    public static IdBrand from(UUID id){
        return new IdBrand(id);
    }
}
