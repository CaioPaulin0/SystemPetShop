package com.SystemPetShop.distributor.application;

import com.SystemPetShop.distributor.application.input.DistributorInput;
import com.SystemPetShop.distributor.application.output.DistributorOutput;
import com.SystemPetShop.distributor.domain.Distributor;
import com.SystemPetShop.distributor.domain.DistributorRepository;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.DistributorCreatedEvent;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce.DistributorProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DistributorSaveUseCase {
    private final DistributorRepository distributorRepository;
    private final DistributorProduce distributorProduce;

    public DistributorSaveUseCase(DistributorRepository distributorRepository, DistributorProduce distributorProduce){
        this.distributorRepository = distributorRepository;
        this.distributorProduce = distributorProduce;
    }

    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public DistributorOutput execute(DistributorInput distributorInput){
        Distributor distributor = new Distributor(
                distributorInput.CNPJ(),
                distributorInput.typeDistributor(),
                distributorInput.nameDistributor(),
                distributorInput.localizationDistributor(),
                distributorInput.telephones(),
                distributorInput.salespeopleDistributor()
        );

        var result = distributorRepository.save(distributor);

        distributorProduce.publish(DistributorCreatedEvent.toEvent(distributor));
        distributorProduce.publishMetrics(DistributorCreatedEvent.toEvent(distributor));

                return DistributorOutput.from(result);
    }



}
