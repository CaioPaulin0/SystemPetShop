package com.SystemPetShop.distributor.infrastructure.messaging.kafka.dto;

import java.util.UUID;

public record DistributorDeleteEvent(UUID idDistributor) {


    public static DistributorDeleteEvent toEvent(UUID id){
        return new DistributorDeleteEvent(id);
    }
}
