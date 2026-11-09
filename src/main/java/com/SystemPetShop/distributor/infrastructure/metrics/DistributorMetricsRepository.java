package com.SystemPetShop.distributor.infrastructure.metrics;

import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.metrics.infrastructure.repository.PriceTotals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;
import java.util.List;

public interface DistributorMetricsRepository extends JpaRepository<DistributorEntity, UUID> {
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    List<DistributorEntity> findByNameDistributorContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
            SELECT new com.SystemPetShop.metrics.infrastructure.repository.PriceTotals(
                COALESCE(SUM(p.priceBuy), 0.0), COALESCE(SUM(p.priceSell), 0.0))
            FROM ProductEntity p
            WHERE p.idDistributor = :idDistributor AND p.productEnableDue = true
            """)
    PriceTotals metricsDistributor(@Param("idDistributor") UUID idDistributor);
}
