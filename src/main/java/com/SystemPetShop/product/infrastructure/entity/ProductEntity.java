package com.SystemPetShop.product.infrastructure.entity;

import com.SystemPetShop.product.domain.IdProduct;
import com.SystemPetShop.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class ProductEntity {
    @Id
    private UUID idProduct;

    @Column(nullable = false, name = "id_brand")
    private UUID idBrand;

    @Column(nullable = false, name = "id_distributor")
    private UUID idDistributor;

    @Column(nullable = false,name = "name_product", unique = true)
    private String nameProduct;

    @Column(nullable = false, name = "type_product")
    private String typeProduct;

    @Column(nullable = false, name = "description_product")
    private String descriptionProduct;

    @Column(nullable = false, name = "data_created")
    private LocalDate dataCreated;

    @Column(nullable = false, name = "data_created_product")
    private LocalDate dataCreatedProduct;

    @Column(nullable = false, name = "data_due_product ")
    private LocalDate dataDueProduct;

    @Column(nullable = false, name = "lot_product")
    private String lotProduct;

    @Column(nullable = false, name = "price_buy")
    private double priceBuy;

    @Column(nullable = false, name = "price_sell")
    private double priceSell;

    @Column(nullable = false, name = "stock_product")
    private long stockProduct;

    @Column(nullable = false, name = "data_due_product_verify")
    private boolean dataDueProductVerify;

    @Column(nullable = false, name = "version_product")
    private long versionProduct;

    @Column(nullable = false, name = "product_enable_due")
    private boolean productEnableDue;

    public static ProductEntity from(Product product){
        return new ProductEntity(
                product.getIdProduct().id(),
                product.getIdBrand(),
                product.getIdDistributor(),
                product.getNameProduct(),
                product.getTypeProduct(),
                product.getDescriptionProduct(),
                product.getDataCreated(),
                product.getDataCreatedProduct(),
                product.getDataDueProduct(),
                product.getLotProduct(),
                product.getPriceBuy(),
                product.getPriceSell(),
                product.getStockProduct(),
                product.isDataDueProductVerify(),
                product.getVersionProduct(),
                product.isProductEnableDue()
        );
    }

    public Product toDomain(){
        return new Product(
                new IdProduct(this.idProduct),
                this.idBrand,
                this.idDistributor,
                this.nameProduct,
                this.typeProduct,
                this.descriptionProduct,
                this.dataCreated,
                this.dataCreatedProduct,
                this.dataDueProduct,
                this.lotProduct,
                this.priceBuy,
                this.priceSell,
                this.stockProduct,
                this.dataDueProductVerify,
                this.versionProduct,
                this.productEnableDue
        );
    }
}
