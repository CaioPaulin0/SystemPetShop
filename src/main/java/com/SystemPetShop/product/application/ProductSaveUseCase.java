package com.SystemPetShop.product.application;

import com.SystemPetShop.product.application.input.ProductInput;
import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductCreatedProduce;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductCreatedMetricsProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductSaveUseCase {
    private final ProductRepository productRepository;
    private final ProductCreatedProduce productCreatedProduce;
    private final ProductCreatedMetricsProduce productCreatedMetricsProduce;

    public ProductSaveUseCase(ProductRepository productRepository, ProductCreatedProduce productCreatedProduce,
                              ProductCreatedMetricsProduce productCreatedMetricsProduce) {
        this.productRepository = productRepository;
        this.productCreatedProduce = productCreatedProduce;
        this.productCreatedMetricsProduce = productCreatedMetricsProduce;
    }


    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public ProductOutput execute(ProductInput productInput){
        var result = productRepository.save(
                new Product(
                        productInput.idBrand(),
                        productInput.idDistributor(),
                        productInput.nameProduct(),
                        productInput.typeProduct(),
                        productInput.descriptionProduct(),
                        productInput.dataCreatedProduct(),
                        productInput.dataDueProduct(),
                        productInput.lotProduct(),
                        productInput.priceBuy(),
                        productInput.priceSell(),
                        productInput.stockProduct(),
                        productInput.dataDueProductVerify(),
                        productInput.versionProduct(),
                        true
                )
        );

        var event = ProductCreatedEvent.toEvent(result);
        productCreatedProduce.execute(event);
        productCreatedMetricsProduce.execute(event);

        return ProductOutput.from(result);
    }
}
