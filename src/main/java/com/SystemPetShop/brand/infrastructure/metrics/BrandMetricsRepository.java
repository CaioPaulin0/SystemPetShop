package com.SystemPetShop.brand.infrastructure.metrics;

import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;
import com.SystemPetShop.metrics.infrastructure.repository.PriceTotals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface BrandMetricsRepository extends JpaRepository<BrandEntity, UUID> {
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    List<BrandEntity> findByNameBrandContainingIgnoreCase(String name, Pageable pageable);

    @Modifying
    @Transactional(transactionManager = "metricsTransactionManager")
    @Query("""
            UPDATE BrandEntity brand
            SET brand.nameBrand = :#{#brand.nameBrand},
                brand.typeBrand = :#{#brand.typeBrand}
            WHERE brand.idBrand = :#{#brand.idBrand}
            """)
    void editBrand(@Param("brand") BrandEntity brandEntity);

    @Query("""
            SELECT new com.SystemPetShop.metrics.infrastructure.repository.PriceTotals(
                COALESCE(SUM(p.priceBuy), 0.0), COALESCE(SUM(p.priceSell), 0.0))
            FROM ProductEntity p
            WHERE p.idBrand = :idBrand AND p.productEnableDue = true
            """)
    PriceTotals metricsBrand(@Param("idBrand") UUID idBrand);

    @Query("""
    SELECT b
    FROM BrandEntity b
    WHERE b.idDistributor = :idDistributor
    ORDER BY b.idBrand
""")
    List<BrandEntity> findAllByDistributorTotal(@Param("idDistributor") UUID idDistributor);
}
