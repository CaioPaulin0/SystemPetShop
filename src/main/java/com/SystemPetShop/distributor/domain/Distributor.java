package com.SystemPetShop.distributor.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Distributor {

    public IdDistributor id;
    private String nameDistributor;
    private String localizationDistributor;
    private List<String> salespeopleDistributor;
    private String typeDistributor;
    private List<String> telephones;
    private String CNPJ;

    public Distributor(String CNPJ,
                       String typeDistributor,
                       String nameDistributor,
                       String localizationDistributor,
                       List<String> telephones,
                       List<String> salespeopleDistributor) {
        this.id = new IdDistributor();
        this.CNPJ = CNPJ;
        this.typeDistributor = typeDistributor;
        this.nameDistributor = nameDistributor;
        this.localizationDistributor = localizationDistributor;
        this.telephones = telephones;
        this.salespeopleDistributor = salespeopleDistributor;
    }

}
