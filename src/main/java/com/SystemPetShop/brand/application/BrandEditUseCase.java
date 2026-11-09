package com.SystemPetShop.brand.application;

import com.SystemPetShop.brand.application.input.BrandEditInput;
import com.SystemPetShop.brand.domain.Brand;
import com.SystemPetShop.brand.domain.BrandRepository;
import com.SystemPetShop.brand.domain.IdBrand;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandEditProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandEditMetricsProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandEditUseCase {
    private final BrandRepository brandRepository;
    private final BrandEditProduce brandEditProduce;
    private final BrandEditMetricsProduce brandEditMetricsProduce;

    public BrandEditUseCase(BrandRepository brandRepository, BrandEditProduce brandEditProduce,
                           BrandEditMetricsProduce brandEditMetricsProduce) {
        this.brandRepository = brandRepository;
        this.brandEditProduce = brandEditProduce;
        this.brandEditMetricsProduce = brandEditMetricsProduce;
    }


    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public String execute(BrandEditInput brandEditInput) throws Exception {
            if(!brandRepository.existsById(brandEditInput.idBrand())){
                throw new Exception("Brand is null");
            }


            Brand brand = new Brand(
                    brandEditInput.idDistributor(),
                    brandEditInput.nameBrand(),
                    brandEditInput.typeBrand()
            );

            brand.setIdBrand(IdBrand.from(brandEditInput.idBrand()));
            var result = brandRepository.editBrand(brand);
            var event = BrandCreatedEvent.toEvent(brand);
            brandEditProduce.execute(event);
            brandEditMetricsProduce.execute(event);
            return result;
    }
}
