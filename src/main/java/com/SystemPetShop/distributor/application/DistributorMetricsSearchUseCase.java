package com.SystemPetShop.distributor.application;

import com.SystemPetShop.distributor.application.output.DistributorOutput;
import com.SystemPetShop.distributor.domain.DistributorRepository;
import com.SystemPetShop.shared.application.NameSearch;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributorMetricsSearchUseCase {
    private final DistributorRepository distributorRepository;

    public DistributorMetricsSearchUseCase(DistributorRepository distributorRepository) {
        this.distributorRepository = distributorRepository;
    }

    public List<DistributorOutput> execute(String name, int page) {
        return NameSearch.execute(name, page, Sort.by("nameDistributor", "idDistributor"),
                distributorRepository::searchMetricsByName, DistributorOutput::from);
    }
}

