package com.SystemPetShop.product.domain;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductRepository {
    List<Product> searchByName(String name, Pageable pageable);
    Product save(Product product);
    Product findById(UUID id);
    List<Product> findAllByProduct(UUID idBrand, Pageable pageable);
    List<Product> searchMetricsByName(String name, Pageable pageable);
    String editProduct(Product product);
    String delete(UUID id);
    Boolean existsById(UUID idProduct);
    Product newVersion(Product product);
    List<Product> metricsProduct(UUID idBrand, Pageable pageable);
    int changeValueProductEnable(UUID idProduct);
    boolean existLot(Product product);
}
