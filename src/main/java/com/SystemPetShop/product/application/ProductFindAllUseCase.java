package com.SystemPetShop.product.application;

import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductFindAllUseCase {
    private final ProductRepository productRepository;

    public ProductFindAllUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductOutput> execute(UUID idBrand, int page) {
        if(idBrand == null){
            throw new RuntimeException("ID is null");
        }

        var result = productRepository.findAllByProduct(idBrand, PageRequest.of(page, 6));

        return result.stream().map(p -> ProductOutput.from(p)).toList();
    }
}
