package com.SystemPetShop.distributor.infrastructure.http.response;

import com.SystemPetShop.distributor.application.output.DistributorOutput;

import java.util.List;

public record DistributorReponse(
        String id,
        String nameDistributor,
        String localizationDistributor,
        List<String> salespeopleDistributor,
        String typeDistributor,
        List<String> telephones,
        String CNPJ
) {
    public static DistributorReponse from(DistributorOutput distributorOutput){
        return new DistributorReponse(
                distributorOutput.id(),
                distributorOutput.nameDistributor(),
                distributorOutput.localizationDistributor(),
                distributorOutput.salespeopleDistributor(),
                distributorOutput.typeDistributor(),
                distributorOutput.telephones(),
                distributorOutput.CNPJ()
        );
    }
}
