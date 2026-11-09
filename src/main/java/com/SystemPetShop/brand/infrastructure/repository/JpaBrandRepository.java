package com.SystemPetShop.brand.infrastructure.repository;

import com.SystemPetShop.brand.domain.Brand;
import com.SystemPetShop.brand.domain.BrandRepository;
import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;
import com.SystemPetShop.brand.infrastructure.metrics.BrandMetricsRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class JpaBrandRepository implements BrandRepository {
    private final BrandEntityRepository brandEntityRepository;
    private final BrandMetricsRepository brandMetricsRepository;

    public JpaBrandRepository(BrandEntityRepository brandEntityRepository, BrandMetricsRepository brandMetricsRepository) {
        this.brandEntityRepository = brandEntityRepository;
        this.brandMetricsRepository = brandMetricsRepository;
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    public List<Brand> searchMetricsByName(String name, Pageable pageable) {
        return brandMetricsRepository.findByNameBrandContainingIgnoreCase(name, pageable).stream()
                .map(BrandEntity::toBrand)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    public List<Brand> searchByName(String name, Pageable pageable) {
        return brandEntityRepository.findByNameBrandContainingIgnoreCase(name, pageable).stream()
                .map(BrandEntity::toBrand)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Brand save(Brand brand) {
        return brandEntityRepository.save(BrandEntity.from(brand)).toBrand();
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager")
    public List<Brand> findAllByDistributorTotal(UUID idDistributor) {
        return brandMetricsRepository.findAllByDistributorTotal(idDistributor).stream()
                .map(BrandEntity::toBrand)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public List<Brand> findAllByDistributor(UUID idDistributor, Pageable pageable) {
        if(idDistributor == null){
            throw new RuntimeException("idDistributor is null");
        }

        return brandEntityRepository.findAllByDistributor(idDistributor, pageable)
                .stream()
                .map(BrandEntity::toBrand)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager")
    public List<Brand> findAllByDistributorMetrics(UUID idDistributor, Pageable pageable) {
        if(idDistributor == null){
            throw new RuntimeException("idDistributor is null");
        }

        return brandEntityRepository.findAllByDistributor(idDistributor, pageable)
                .stream()
                .map(BrandEntity::toBrand)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    public List<Double> metricsBrand(UUID idBrand) {
        var totals = brandMetricsRepository.metricsBrand(idBrand);
        return List.of(totals.totalBuy(), totals.totalSell());
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public String deletBrand(UUID brand) {
         brandEntityRepository.deleteById(brand);

        return "delet successfull";
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public String editBrand(Brand brand) {
        brandEntityRepository.editBrand(BrandEntity.from(brand));

        return "Atualizado com sucesso!";
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Boolean existsById(UUID idBrand) {
        return brandEntityRepository.existsById(idBrand);
    }

}
