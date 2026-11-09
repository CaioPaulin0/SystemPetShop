package com.SystemPetShop.distributor.application.input;

import java.util.List;

public record DistributorInput(
        String nameDistributor,
        String localizationDistributor,
        List<String> salespeopleDistributor,
        String typeDistributor,
        List<String> telephones,
        String CNPJ
){}