package com.SystemPetShop.expired.infrastructure.entity;

import com.SystemPetShop.expired.domain.Expired;
import com.SystemPetShop.product.infrastructure.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "product_entity")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ExpiredEntity {
    @Id
    private UUID idProduct;

    @Column(nullable = false, name = "id_brand")
    private UUID idBrand;

    @Column(nullable = false, name = "id_distributor")
    private UUID idDistributor;

    @Column(nullable = false,name = "name_product", unique = true)
    private String nameProduct;

    @Transient
    private String nameDistributor;

    @Transient
    private String nameBrand;

    @Column(nullable = false, name = "data_created")
    private LocalDate dataCreated;

    @Column(nullable = false, name = "data_created_product")
    private LocalDate dataCreatedProduct;

    @Column(nullable = false, name = "data_due_product")
    private LocalDate dataDueProduct;

    @Column(nullable = false, name = "lot_product")
    private String lotProduct;

    @Column(nullable = false, name = "stock_product")
    private long stockProduct;

    @Column(nullable = false, name = "data_due_product_verify")
    private boolean dataDueProductVerify;

    @Column(nullable = false, name = "product_enable_due")
    private boolean productEnableDue;

    public static ExpiredEntity fromProduct(ProductEntity product, String nameBrand, String nameDistributor) {
        return new ExpiredEntity(
                product.getIdProduct(),
                product.getIdBrand(),
                product.getIdDistributor(),
                product.getNameProduct(),
                nameDistributor,
                nameBrand,
                product.getDataCreated(),
                product.getDataCreatedProduct(),
                product.getDataDueProduct(),
                product.getLotProduct(),
                product.getStockProduct(),
                product.isDataDueProductVerify(),
                product.isProductEnableDue()
        );
    }

    public static ExpiredEntity from(Expired expired){
        return new ExpiredEntity(
                expired.getIdProduct(),
                expired.getIdBrand(),
                expired.getIdDistributor(),
                expired.getNameProduct(),
                expired.getNameDistributor(),
                expired.getNameBrand(),
                expired.getDataCreated(),
                expired.getDataCreatedProduct(),
                expired.getDataDueProduct(),
                expired.getLotProduct(),
                expired.getStockProduct(),
                expired.isDataDueProductVerify(),
                expired.isProductEnableDue()
        );
    }

    public Expired toDomain(){
        return new Expired(
                this.idBrand,
        this.idProduct,
        this.idDistributor,
        this.dataDueProductVerify,
        this.lotProduct,
        this.nameProduct,
        this.nameDistributor,
        this.dataCreatedProduct,
        this.dataDueProduct,
        this.dataCreated,
        this.nameBrand,
        this.stockProduct,
        this.productEnableDue
        );
    }

}
