package com.SystemPetShop.distributor.application;

import com.SystemPetShop.distributor.domain.DistributorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DistributorMetricsUseCase {

    private final DistributorRepository distributorRepository;

    public DistributorMetricsUseCase(DistributorRepository distributorRepository) {
        this.distributorRepository = distributorRepository;
    }

    public List<Double> execute(UUID idDistributor) {
        return distributorRepository.metricsDistributor(idDistributor);
    }

}
