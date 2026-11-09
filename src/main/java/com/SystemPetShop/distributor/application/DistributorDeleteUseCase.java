package com.SystemPetShop.distributor.application;

import com.SystemPetShop.distributor.domain.DistributorRepository;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce.DistributorProduce;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.dto.DistributorDeleteEvent;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce.DistributorDeleteProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DistributorDeleteUseCase {
    private final DistributorRepository distributorRepository;
    private final DistributorDeleteProduce distributorDeleteProduce;
    public DistributorDeleteUseCase(DistributorRepository distributorRepository, DistributorDeleteProduce distributorDeleteProduce) {
        this.distributorRepository = distributorRepository;
        this.distributorDeleteProduce = distributorDeleteProduce;
    }

    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public String execute(UUID id) throws Exception {
        if(!distributorRepository.existsById(id)){
            throw new Exception("Distributor is null");
        }

        try{
        distributorDeleteProduce.delete(DistributorDeleteEvent.toEvent(id));
        distributorDeleteProduce.deleteMetrics(DistributorDeleteEvent.toEvent(id));
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }

        return distributorRepository.delete(id);
    }
}
