package com.SystemPetShop.product.application;

import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductMetricUseCase {

    private final ProductRepository productRepository;

    public ProductMetricUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductOutput> execute(UUID idBrand, int page){
        if(idBrand == null){
            throw new RuntimeException("idBrand is empty");
        }
        return productRepository.metricsProduct(idBrand, PageRequest.of(page, 12)).stream()
                .map(e -> ProductOutput.from(e))
                .toList();
    }
}
