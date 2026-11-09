package com.SystemPetShop.distributor.application.input;

import java.util.List;
import java.util.UUID;

public record DistributorEditInput(
        UUID idDistributor,
        String nameDistributor,
        String localizationDistributor,
        List<String> salespeopleDistributor,
        String typeDistributor,
        List<String> telephones,
        String CNPJ
) {
}
