package com.SystemPetShop.distributor.application;

import com.SystemPetShop.distributor.application.output.DistributorOutput;
import com.SystemPetShop.distributor.domain.DistributorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributorFindAllUseCase {
    private final DistributorRepository distributorRepository;

    public DistributorFindAllUseCase(DistributorRepository distributorRepository) {
        this.distributorRepository = distributorRepository;
    }

    public List<DistributorOutput> execute(int page) throws Exception {
        var result = distributorRepository.findAllDistributor(PageRequest.of(page, 6));

        if(result.isEmpty()){
            throw new Exception("list was empty");
        }

        return result.stream()
                .map(DistributorOutput::from)
                .toList();
    }

    public List<DistributorOutput> executeMetrics(int page) throws Exception {
        var result = distributorRepository.findAllDistributorMetrics(PageRequest.of(page, 9));

        if(result.isEmpty()){
            throw new Exception("list was empty");
        }

        return result.stream()
                .map(DistributorOutput::from)
                .toList();
    }

    public List<DistributorOutput> executeMetricsTotal() throws Exception {
        var result = distributorRepository.findAllDistributorMetricsTotal();

        if(result.isEmpty()){
            throw new Exception("list was empty");
        }

        return result.stream()
                .map(DistributorOutput::from)
                .toList();
    }
}
