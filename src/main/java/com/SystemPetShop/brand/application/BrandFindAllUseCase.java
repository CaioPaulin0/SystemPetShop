package com.SystemPetShop.brand.application;

import com.SystemPetShop.brand.application.output.BrandOutput;
import com.SystemPetShop.brand.domain.BrandRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BrandFindAllUseCase {
    private final BrandRepository brandRepository;

    public BrandFindAllUseCase(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<BrandOutput> execute(UUID brandFindAllInput, int page) throws Exception {

        if(brandFindAllInput == null){
            throw new Exception("brand is null");
        }

        return brandRepository.findAllByDistributor(brandFindAllInput, PageRequest.of(page, 6)).stream()
                .map(BrandOutput::from)
                .toList();

    }

    public List<BrandOutput> executeMetrics(UUID brandFindAllInput, int page) throws Exception {

        if(brandFindAllInput == null){
            throw new Exception("brand is null");
        }

        return brandRepository.findAllByDistributorMetrics(brandFindAllInput, PageRequest.of(page, 9)).stream()
                .map(BrandOutput::from)
                .toList();

    }
}
