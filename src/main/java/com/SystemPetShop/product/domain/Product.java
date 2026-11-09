package com.SystemPetShop.product.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Product {
    public IdProduct idProduct;
    private UUID idBrand;
    private UUID idDistributor;
    private String nameProduct;
    private String typeProduct;
    private String descriptionProduct;
    private LocalDate dataCreated;
    private LocalDate dataCreatedProduct;
    private LocalDate dataDueProduct;
    private String lotProduct;
    private double priceBuy;
    private double priceSell;
    private long stockProduct;
    private boolean dataDueProductVerify;
    private long versionProduct;
    private boolean productEnableDue;

    public Product(UUID idBrand,
                   UUID idDistributor,
                   String nameProduct,
                   String typeProduct,
                   String descriptionProduct,
                   LocalDate dataCreatedProduct,
                   LocalDate dataDueProduct,
                   String lotProduct,
                   double priceBuy,
                   double priceSell,
                   long stockProduct,
                   boolean dataDueProductVerify,
                   long versionProduct,
                   boolean productEnableDue
                   ) {
        this.idProduct = new IdProduct();
        this.idBrand = idBrand;
        this.idDistributor = idDistributor;
        this.nameProduct = nameProduct;
        this.typeProduct = typeProduct;
        this.descriptionProduct = descriptionProduct;
        this.dataCreated = LocalDate.now();
        this.dataCreatedProduct = dataCreatedProduct;
        this.dataDueProduct = dataDueProduct;
        this.lotProduct = lotProduct;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.stockProduct = stockProduct;
        this.dataDueProductVerify = dataDueProductVerify;
        this.versionProduct = versionProduct;
        this.productEnableDue = productEnableDue;
    }
}
