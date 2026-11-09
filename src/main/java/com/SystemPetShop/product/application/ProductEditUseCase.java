package com.SystemPetShop.product.application;

import com.SystemPetShop.product.application.input.ProductEditInput;
import com.SystemPetShop.product.domain.IdProduct;
import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductEditProduce;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.ProductEditMetricsProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductEditUseCase {
    private final ProductRepository productRepository;
    private final ProductEditProduce productEditProduce;
    private final ProductEditMetricsProduce productEditMetricsProduce;

    public ProductEditUseCase(ProductRepository productRepository, ProductEditProduce productEditProduce,
                              ProductEditMetricsProduce productEditMetricsProduce) {
        this.productRepository = productRepository;
        this.productEditProduce = productEditProduce;
        this.productEditMetricsProduce = productEditMetricsProduce;
    }

    @Transactional(transactionManager = "mainTransactionManager", rollbackFor = Exception.class)
    public String execute(ProductEditInput productEditInput) throws Exception {

            if(!productRepository.existsById(productEditInput.idProduct())){
                throw new Exception("product is null");
            }

            var entity = productRepository.findById(productEditInput.idProduct());
            if(entity == null){
                throw new Exception("entity is empty");
            }

            entity.setIdBrand(productEditInput.idBrand());
            entity.setIdDistributor(productEditInput.idDistributor());
            entity.setTypeProduct(productEditInput.typeProduct());
            entity.setDescriptionProduct(productEditInput.descriptionProduct());
            entity.setDataCreatedProduct(productEditInput.dataCreatedProduct());
            entity.setDataDueProduct(productEditInput.dataDueProduct());
            entity.setLotProduct(productEditInput.lotProduct());
            entity.setPriceBuy(productEditInput.priceBuy());
            entity.setPriceSell(productEditInput.priceSell());
            entity.setStockProduct(productEditInput.stockProduct());

            entity.setIdProduct(IdProduct.toEdit(productEditInput.idProduct()));

            var result = productRepository.editProduct(entity);
            var event = ProductCreatedEvent.toEvent(entity);
            productEditProduce.execute(event);
            productEditMetricsProduce.execute(event);
            return result;

    }
}
