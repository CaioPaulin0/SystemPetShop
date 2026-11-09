package com.SystemPetShop.distributor.infrastructure.http.dto;

import com.SystemPetShop.distributor.application.input.DistributorInput;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent;

import java.util.List;

public record DistributorDto(
        String nameDistributor,
        String localizationDistributor,
        List<String> salespeopleDistributor,
        String typeDistributor,
        List<String> telephones,
        String CNPJ
) {

    public DistributorInput toInput(){
        return new DistributorInput(
                nameDistributor,
                localizationDistributor,
                salespeopleDistributor,
                typeDistributor,
                telephones,
                CNPJ
        );
    }

}