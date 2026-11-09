package com.SystemPetShop.brand.application;

import com.SystemPetShop.brand.application.output.BrandOutput;
import com.SystemPetShop.brand.domain.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BrandMetricsUseCase {
    private final BrandRepository brandRepository;

    public BrandMetricsUseCase(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Double> execute(UUID idBrand) {
        return brandRepository.metricsBrand(idBrand);
    }

    public List<BrandOutput> findByMetricsTotal(UUID idDistributor) throws Exception {
        if(idDistributor == null){
            throw new Exception("brand is null");
        }

        return brandRepository.findAllByDistributorTotal(idDistributor).stream().map(BrandOutput::from).toList();
    }
}
