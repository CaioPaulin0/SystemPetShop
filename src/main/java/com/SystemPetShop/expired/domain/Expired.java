package com.SystemPetShop.expired.domain;

import com.SystemPetShop.product.domain.IdProduct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Expired {
    public UUID idProduct;
    private UUID idBrand;
    private UUID idDistributor;
    private String nameProduct;
    private String nameDistributor;
    private String nameBrand;
    private LocalDate dataCreated;
    private LocalDate dataCreatedProduct;
    private LocalDate dataDueProduct;
    private String lotProduct;
    private long stockProduct;
    private boolean dataDueProductVerify;
    private boolean productEnableDue;

    public Expired(UUID idBrand, UUID idProduct, UUID idDistributor, boolean dataDueProductVerify, String lotProduct, String nameProduct, String nameDistributor, LocalDate dataCreatedProduct, LocalDate dataDueProduct, LocalDate dataCreated, String nameBrand,long stockProduct, boolean productEnableDue) {
        this.idBrand = idBrand;
        this.idProduct = idProduct;
        this.idDistributor = idDistributor;
        this.dataDueProductVerify = dataDueProductVerify;
        this.lotProduct = lotProduct;
        this.nameProduct = nameProduct;
        this.nameDistributor = nameDistributor;
        this.dataCreatedProduct = dataCreatedProduct;
        this.dataDueProduct = dataDueProduct;
        this.dataCreated = dataCreated;
        this.nameBrand = nameBrand;
        this.stockProduct = stockProduct;
        this.productEnableDue = productEnableDue;
    }
}
