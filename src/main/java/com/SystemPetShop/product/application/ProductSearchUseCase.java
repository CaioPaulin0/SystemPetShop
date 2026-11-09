package com.SystemPetShop.product.application;

import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.shared.application.NameSearch;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductSearchUseCase {
    private final ProductRepository productRepository;

    public ProductSearchUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductOutput> execute(String name, int page) {
        return NameSearch.execute(name, page, Sort.by("nameProduct", "idProduct"),
                productRepository::searchByName, ProductOutput::from);
    }
}
