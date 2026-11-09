package com.SystemPetShop.brand.infrastructure.repository;

import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface BrandEntityRepository extends JpaRepository<BrandEntity, UUID> {

    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    List<BrandEntity> findByNameBrandContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
    SELECT b
    FROM BrandEntity b
    WHERE b.idDistributor = :idDistributor
    ORDER BY b.idBrand
""")
    List<BrandEntity> findAllByDistributor(@Param("idDistributor") UUID idDistributor, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""        
            UPDATE BrandEntity brand 
            SET brand.nameBrand = :#{#brand.nameBrand},
                brand.typeBrand = :#{#brand.typeBrand}
            WHERE brand.idBrand = :#{#brand.idBrand}  
            """)
    void editBrand(@Param("brand") BrandEntity brandEntity);

}
