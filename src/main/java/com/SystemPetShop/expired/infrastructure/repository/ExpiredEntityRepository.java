package com.SystemPetShop.expired.infrastructure.repository;

import com.SystemPetShop.expired.infrastructure.entity.ExpiredEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpiredEntityRepository extends JpaRepository<ExpiredEntity, UUID> {

    @Transactional(transactionManager = "expiredTransactionManager", readOnly = true)
    @Query("""
            SELECT p AS product, b.nameBrand AS nameBrand, d.nameDistributor AS nameDistributor
            FROM ProductEntity p
            LEFT JOIN BrandEntity b ON b.idBrand = p.idBrand
            LEFT JOIN DistributorEntity d ON d.idDistributor = p.idDistributor
            WHERE p.versionProduct = (
                SELECT MAX(p2.versionProduct)
                FROM ProductEntity p2
                WHERE p2.lotProduct = p.lotProduct
            )
            AND p.dataDueProductVerify = :dataDueProductVerify
            """)
    List<ExpiredProductProjection> filterByDataDueProductVerify(
            @Param("dataDueProductVerify") boolean dataDueProductVerify, Pageable pageable);

    @Transactional(transactionManager = "expiredTransactionManager", readOnly = true)
    @Query("""
            SELECT p AS product, b.nameBrand AS nameBrand, d.nameDistributor AS nameDistributor
            FROM ProductEntity p
            LEFT JOIN BrandEntity b ON b.idBrand = p.idBrand
            LEFT JOIN DistributorEntity d ON d.idDistributor = p.idDistributor
            WHERE p.versionProduct = (
                SELECT MAX(p2.versionProduct)
                FROM ProductEntity p2
                WHERE p2.lotProduct = p.lotProduct
            )
            AND LOWER(p.nameProduct) LIKE LOWER(CONCAT('%', :escapedName, '%')) ESCAPE '!'
            """)
    List<ExpiredProductProjection> searchByName(@Param("escapedName") String escapedName, Pageable pageable);

    @Query("""
            SELECT p AS product, b.nameBrand AS nameBrand, d.nameDistributor AS nameDistributor
            FROM ProductEntity p
            LEFT JOIN BrandEntity b ON b.idBrand = p.idBrand
            LEFT JOIN DistributorEntity d ON d.idDistributor = p.idDistributor
            WHERE p.idProduct = :idProduct
            """)
    Optional<ExpiredProductProjection> findExpiredByIdProduct(@Param("idProduct") UUID idProduct);

    @Query("""
    SELECT p AS product,
           b.nameBrand AS nameBrand,
           d.nameDistributor AS nameDistributor
    FROM ProductEntity p
    LEFT JOIN BrandEntity b ON b.idBrand = p.idBrand
    LEFT JOIN DistributorEntity d ON d.idDistributor = p.idDistributor
    WHERE p.versionProduct = (
        SELECT MAX(p2.versionProduct)
        FROM ProductEntity p2
        WHERE p2.lotProduct = p.lotProduct
    )
    ORDER BY p.idProduct
""")
    List<ExpiredProductProjection> findAllExpiredProduct(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE ProductEntity p SET p.dataDueProductVerify = true WHERE p.idProduct = :idProduct")
    void verifyDueProduct(@Param("idProduct") UUID idProduct);
}
