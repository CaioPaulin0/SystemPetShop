package com.SystemPetShop.brand.infrastructure.messaging.kafka.repository;

import com.SystemPetShop.brand.infrastructure.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface BrandExpiredRepositoryEntity extends JpaRepository<BrandEntity, UUID> {
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
