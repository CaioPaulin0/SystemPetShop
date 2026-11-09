package com.SystemPetShop.distributor.domain;

import java.util.UUID;

public record IdDistributor(UUID id) {
    public IdDistributor(){
        this(UUID.randomUUID());
    }

    public static IdDistributor from(UUID id){
        return new IdDistributor(id);
    }
}
