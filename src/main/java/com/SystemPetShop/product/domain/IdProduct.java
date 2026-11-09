package com.SystemPetShop.product.domain;

import java.util.UUID;

public record IdProduct(UUID id) {
    public IdProduct(){
        this(UUID.randomUUID());
    }

    public static IdProduct toEdit(UUID id){
        return new IdProduct(id);
    }
}
