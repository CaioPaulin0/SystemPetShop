package com.SystemPetShop.brand.application;

import com.SystemPetShop.brand.application.output.BrandOutput;
import com.SystemPetShop.brand.domain.BrandRepository;
import com.SystemPetShop.shared.application.NameSearch;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandSearchUseCase {
    private final BrandRepository brandRepository;

    public BrandSearchUseCase(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<BrandOutput> execute(String name, int page) {
        return NameSearch.execute(name, page, Sort.by("nameBrand", "idBrand"),
                brandRepository::searchByName, BrandOutput::from);
    }
}
