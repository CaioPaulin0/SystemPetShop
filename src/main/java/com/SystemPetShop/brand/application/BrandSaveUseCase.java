package com.SystemPetShop.brand.application;

import com.SystemPetShop.brand.application.input.BrandInput;
import com.SystemPetShop.brand.application.output.BrandOutput;
import com.SystemPetShop.brand.domain.Brand;
import com.SystemPetShop.brand.domain.BrandRepository;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandCreateProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandCreateMetricsProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandSaveUseCase {
    private final BrandRepository brandRepository;
    private final BrandCreateProduce brandCreateProduce;
    private final BrandCreateMetricsProduce brandCreateMetricsProduce;

    public BrandSaveUseCase(BrandRepository brandRepository, BrandCreateProduce brandCreateProduce,
                            BrandCreateMetricsProduce brandCreateMetricsProduce) {
        this.brandRepository = brandRepository;
        this.brandCreateProduce = brandCreateProduce;
        this.brandCreateMetricsProduce = brandCreateMetricsProduce;
    }


    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public BrandOutput execute(BrandInput brandInput){
        var result = brandRepository.save(new Brand(
                brandInput.idDistributor(),
                brandInput.nameBrand(),
                brandInput.typeBrand()
        ));

        var event = BrandCreatedEvent.toEvent(result);
        brandCreateProduce.execute(event);
        brandCreateMetricsProduce.execute(event);

        return BrandOutput.from(result);
    }
}
