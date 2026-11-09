package com.SystemPetShop.distributor.infrastructure.http.dto;

import com.SystemPetShop.distributor.application.input.DistributorEditInput;
import com.SystemPetShop.distributor.application.input.DistributorInput;

import java.util.List;
import java.util.UUID;

public record DistributorEditDto(
        UUID idDistributor,
        String nameDistributor,
        String localizationDistributor,
        List<String> salespeopleDistributor,
        String typeDistributor,
        List<String> telephones,
        String CNPJ
){
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

    public DistributorEditInput toInputEdit(){
        return new DistributorEditInput(
                idDistributor,
                nameDistributor,
                localizationDistributor,
                salespeopleDistributor,
                typeDistributor,
                telephones,
                CNPJ
        );
    }

}