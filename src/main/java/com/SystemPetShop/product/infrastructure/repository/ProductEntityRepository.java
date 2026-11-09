package com.SystemPetShop.product.infrastructure.repository;

import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface ProductEntityRepository extends JpaRepository<ProductEntity, UUID> {

    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    List<ProductEntity> findByNameProductContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM ProductEntity p WHERE p.idProduct = :idProduct")
    Optional<ProductEntity> findById(@Param("idProduct") UUID idProduct);

    @Query("""
            SELECT a
               FROM ProductEntity a
               WHERE a.idBrand = :idBrand
                 AND a.versionProduct = (
                  SELECT MAX(a.versionProduct)
                  FROM ProductEntity b
               WHERE b.idBrand = :idBrand
               
              AND a.productEnableDue = true
            )
            ORDER BY a.idProduct
            """)
    List<ProductEntity> findAllByProduct(@Param("idBrand") UUID idBrand, Pageable pageable);

    @Modifying
    @Transactional
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
    @Transactional
    @Query("DELETE FROM ProductEntity p WHERE p.idProduct = :idProduct")
    void delete(@Param("idProduct") UUID idProduct);

    @Modifying
    @Transactional
    @Query("UPDATE ProductEntity p SET p.productEnableDue = false WHERE p.idProduct = :idProduct")
    int changeValueProductEnable(@Param("idProduct") UUID idProduct);

    @Query(value = """
    SELECT EXISTS(
        SELECT 1
        FROM product_entity
        WHERE lot_product = :#{#product.lotProduct}
        AND id_product = :#{#product.idProduct}
    )
    """, nativeQuery = true)
    Long existsProduct(@Param("product") ProductEntity entity);
}
