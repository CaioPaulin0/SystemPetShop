package com.SystemPetShop.distributor.domain;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DistributorRepository {
    List<Distributor> searchByName(String name, Pageable pageable);
    List<Distributor> searchMetricsByName(String name, Pageable pageable);
    Distributor save(Distributor distributor);
    String delete(UUID id);
    List<Distributor> findAllDistributor(Pageable pageable);
    List<Distributor> findAllDistributorMetrics(Pageable pageable);
    List<Distributor> findAllDistributorMetricsTotal();
    List<Double> metricsDistributor(UUID idDistributor);
    String editDistributor(Distributor distributor);
    Boolean existsById(UUID idDistributor);
}
