package com.SystemPetShop.distributor.application.output;

import com.SystemPetShop.distributor.domain.Distributor;

import java.util.List;

public record DistributorOutput(
        String id,
        String nameDistributor,
        String localizationDistributor,
        List<String> salespeopleDistributor,
        String typeDistributor,
        List<String> telephones,
        String CNPJ
) {
    public static DistributorOutput from(Distributor distributor){
        return new DistributorOutput(distributor.getId().id().toString(),
                distributor.getNameDistributor(),
                distributor.getLocalizationDistributor(),
                distributor.getSalespeopleDistributor(),
                distributor.getTypeDistributor(),
                distributor.getTelephones(),
                distributor.getCNPJ()
        );
    }
}
