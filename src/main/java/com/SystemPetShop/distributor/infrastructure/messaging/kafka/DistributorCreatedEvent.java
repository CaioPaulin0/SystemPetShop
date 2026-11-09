package com.SystemPetShop.distributor.infrastructure.messaging.kafka;

import com.SystemPetShop.distributor.domain.Distributor;
import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;

import java.util.List;
import java.util.UUID;

public record DistributorCreatedEvent(
        UUID idDistributor,
        String nameDistributor,
        String localizationDistributor,
        List<String> salespeopleDistributor,
        String typeDistributor,
        List<String> telephones,
        String CNPJ
) {

    public static DistributorCreatedEvent from(DistributorEntity distributorEntity){
        return new DistributorCreatedEvent(
                distributorEntity.getIdDistributor(),
                distributorEntity.getNameDistributor(),
                distributorEntity.getLocalizationDistributor(),
                distributorEntity.getSalespeopleDistributor(),
                distributorEntity.getTypeDistributor(),
                distributorEntity.getTelephones(),
                distributorEntity.getCNPJ()
        );
    }

    public static DistributorCreatedEvent toEvent(Distributor distributorEntity){
        return new DistributorCreatedEvent(
                distributorEntity.getId().id(),
                distributorEntity.getNameDistributor(),
                distributorEntity.getLocalizationDistributor(),
                distributorEntity.getSalespeopleDistributor(),
                distributorEntity.getTypeDistributor(),
                distributorEntity.getTelephones(),
                distributorEntity.getCNPJ()
        );
    }

}
