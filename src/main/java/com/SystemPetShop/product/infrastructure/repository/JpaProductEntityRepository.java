package com.SystemPetShop.product.infrastructure.repository;

import com.SystemPetShop.metrics.infrastructure.repository.ProductMetricsRepository;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaProductEntityRepository implements ProductRepository {



    private final ProductEntityRepository productEntityRepository;
    private final ProductMetricsRepository productMetricsRepository;

    public JpaProductEntityRepository(ProductEntityRepository productEntityRepository,
                                      ProductMetricsRepository productMetricsRepository) {
        this.productEntityRepository = productEntityRepository;
        this.productMetricsRepository = productMetricsRepository;
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Product save(Product product) {
        var result = ProductEntity.from(product);
        return productEntityRepository.save(result).toDomain();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Product findById(UUID id) {
        var result = productEntityRepository.findById(id).orElseThrow(() -> new RuntimeException("product not found"));
        return result.toDomain();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public List<Product> findAllByProduct(UUID idBrand, Pageable pageable) {
        var result = productEntityRepository.findAllByProduct(idBrand, pageable).stream()
                .map(p -> p.toDomain())
                .toList();

        return result;
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    public List<Product> searchMetricsByName(String name, Pageable pageable) {
        return productMetricsRepository.findByNameProductContainingIgnoreCase(name, pageable).stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    public List<Product> searchByName(String name, Pageable pageable) {
        return productEntityRepository.findByNameProductContainingIgnoreCase(name, pageable).stream()
                .map(ProductEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public String editProduct(Product product) {
        var result = productEntityRepository.editProduct(ProductEntity.from(product));

        return "atualizado com sucesso!";

    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public String delete(UUID id) {
        productEntityRepository.delete(id);

        return "deletado com sucesso!";
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Boolean existsById(UUID idProduct) {
        return productEntityRepository.existsById(idProduct);
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Product newVersion(Product product) {

        var result = productEntityRepository.save(ProductEntity.from(product));

        return result.toDomain();
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    public List<Product> metricsProduct(UUID idBrand, Pageable pageable) {
        return productMetricsRepository.metricsProduct(idBrand, pageable).stream()
                .map(e -> e.toDomain())
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public int changeValueProductEnable(UUID idProduct) {
        return productEntityRepository.changeValueProductEnable(idProduct);
    }

    @Override
    public boolean existLot(Product product) {
        return productEntityRepository.existsProduct(ProductEntity.from(product)) == 1L ;

    }

}
