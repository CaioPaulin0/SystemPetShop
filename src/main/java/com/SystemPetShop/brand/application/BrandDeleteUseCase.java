package com.SystemPetShop.brand.application;

import com.SystemPetShop.brand.domain.BrandRepository;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandDeleteEvent;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandDeleteProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandDeleteMetricsProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BrandDeleteUseCase {
    private final BrandRepository brandRepository;
    private final BrandDeleteProduce brandDeleteProduce;
    private final BrandDeleteMetricsProduce brandDeleteMetricsProduce;

    public BrandDeleteUseCase(BrandRepository brandRepository, BrandDeleteProduce brandDeleteProduce,
                             BrandDeleteMetricsProduce brandDeleteMetricsProduce) {
        this.brandRepository = brandRepository;
        this.brandDeleteProduce = brandDeleteProduce;
        this.brandDeleteMetricsProduce = brandDeleteMetricsProduce;
    }

    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public String execute(UUID idBrand) throws Exception {
        if(!brandRepository.existsById(idBrand)){
            throw new Exception("Brand is null");
        }

        var result = brandRepository.deletBrand(idBrand);
        var event = BrandDeleteEvent.toEvent(idBrand);
        brandDeleteProduce.execute(event);
        brandDeleteMetricsProduce.execute(event);
        return result;
    }
}
