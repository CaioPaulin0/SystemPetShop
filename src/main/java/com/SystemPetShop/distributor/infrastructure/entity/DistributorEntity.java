package com.SystemPetShop.distributor.infrastructure.entity;

import com.SystemPetShop.distributor.domain.Distributor;
import com.SystemPetShop.distributor.domain.IdDistributor;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "distributor_entity")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DistributorEntity {
    @Id
    private UUID idDistributor;

    @Column(nullable = false, name = "name_distributor" , unique = true)
    private String nameDistributor;

    @Column(name = "localization_distributor")
    private String localizationDistributor;

    @Column(name = "salespeople_distributor")
    private List<String> salespeopleDistributor;

    @Column(nullable = false, name = "type_distributor")
    private String typeDistributor;

    @Column(name = "telephones")
    private List<String> telephones;

    @Column(name = "cnpj")
    private String CNPJ;

    public static DistributorEntity from(Distributor distributor){
        return new DistributorEntity(
                distributor.getId().id(),
                distributor.getNameDistributor(),
                distributor.getLocalizationDistributor(),
                distributor.getSalespeopleDistributor(),
                distributor.getTypeDistributor(),
                distributor.getTelephones(),
                distributor.getCNPJ()
                );
    }

    public Distributor toDomain(){
        return new Distributor(
                new IdDistributor(this.idDistributor),
                this.nameDistributor,
                this.localizationDistributor,
                this.salespeopleDistributor,
                this.typeDistributor,
                this.telephones,
                this.CNPJ
        );
    }

}
