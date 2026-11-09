package com.SystemPetShop.brand.domain;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BrandRepository {
    List<Brand> searchByName(String name, Pageable pageable);
        List<Brand> searchMetricsByName(String name, Pageable pageable);
        Brand save(Brand brand);
        List<Brand> findAllByDistributorTotal(UUID idDistributor);
        List<Brand> findAllByDistributor(UUID idDistributor, Pageable pageable);
        List<Brand> findAllByDistributorMetrics(UUID idDistributor, Pageable pageable);
        List<Double> metricsBrand(UUID idBrand);
        String deletBrand(UUID brand);
        String editBrand(Brand brand);
        Boolean existsById(UUID idBrand);
}
