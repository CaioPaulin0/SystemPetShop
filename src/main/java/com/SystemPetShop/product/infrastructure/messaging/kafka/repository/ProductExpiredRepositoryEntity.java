package com.SystemPetShop.product.infrastructure.messaging.kafka.repository;

import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductExpiredRepositoryEntity extends JpaRepository<ProductEntity, UUID> {
    @Modifying
    @Query("""
            UPDATE ProductEntity p
            SET p.dataCreatedProduct = :#{#product.dataCreatedProduct},
                p.dataDueProduct = :#{#product.dataDueProduct},
                p.descriptionProduct = :#{#product.descriptionProduct},
                p.idBrand = :#{#product.idBrand},
                p.idDistributor = :#{#product.idDistributor},
                p.lotProduct = :#{#product.lotProduct},
                p.priceBuy = :#{#product.priceBuy},
                p.priceSell = :#{#product.priceSell},
                p.stockProduct = :#{#product.stockProduct},
                p.typeProduct = :#{#product.typeProduct}
            WHERE p.idProduct = :#{#product.idProduct}
            """)
    int editProduct(@Param("product") ProductEntity entity);

    @Modifying
    @Query("UPDATE ProductEntity p SET p.productEnableDue = false WHERE p.idProduct = :idProduct")
    int disablePreviousVersion(@Param("idProduct") UUID idProduct);
}
