package com.SystemPetShop.product.application;

import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductDeleteProduce;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductDeleteMetricsProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductDeleteUseCase {
    private final ProductRepository productRepository;
    private final ProductDeleteProduce productDeleteProduce;
    private final ProductDeleteMetricsProduce productDeleteMetricsProduce;

    public ProductDeleteUseCase(ProductRepository productRepository, ProductDeleteProduce productDeleteProduce,
                                ProductDeleteMetricsProduce productDeleteMetricsProduce) {
        this.productRepository = productRepository;
        this.productDeleteProduce = productDeleteProduce;
        this.productDeleteMetricsProduce = productDeleteMetricsProduce;
    }

    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public String execute(UUID id) throws Exception {

        if(!productRepository.existsById(id)){
            throw new Exception("product is null");
        }

        productRepository.delete(id);
        var event = ProductDeleteEvent.toEvent(id);
        productDeleteProduce.execute(event);
        productDeleteMetricsProduce.execute(event);

        return "Product Deleted";
    }
}
