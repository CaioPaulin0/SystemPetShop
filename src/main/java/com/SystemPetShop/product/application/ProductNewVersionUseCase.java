package com.SystemPetShop.product.application;

import com.SystemPetShop.product.application.input.ProductNewVersionInput;
import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionMetricsEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductNewVersionProduce;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductNewVersionMetricsProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductNewVersionUseCase {

    private final ProductRepository productRepository;
    private final ProductNewVersionProduce productNewVersionProduce;
    private final ProductNewVersionMetricsProduce productNewVersionMetricsProduce;

    public ProductNewVersionUseCase(ProductRepository productRepository, ProductNewVersionProduce productNewVersionProduce,
                                    ProductNewVersionMetricsProduce productNewVersionMetricsProduce) {
        this.productRepository = productRepository;
        this.productNewVersionProduce = productNewVersionProduce;
        this.productNewVersionMetricsProduce = productNewVersionMetricsProduce;
    }

    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public ProductOutput execute(ProductNewVersionInput productInput){

        try{
            productRepository.changeValueProductEnable(productInput.idProduct());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long newVersionOfProduct = productInput.versionProduct() + 1;
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
                        newVersionOfProduct,
                        true
                )
        );

        if(productRepository.existLot(result)){
            productNewVersionProduce.execute(ProductNewVersionEvent.toEvent(result));
        }
        productNewVersionMetricsProduce.execute(ProductNewVersionMetricsEvent.toEvent(productInput.idProduct(), result));
        return ProductOutput.from(result);
    }
}
