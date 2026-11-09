package com.SystemPetShop.product.application.output;

import com.SystemPetShop.product.domain.IdProduct;
import com.SystemPetShop.product.domain.Product;

import java.time.LocalDate;
import java.util.UUID;

public record ProductDueOutput
        (
         UUID idProduct,
         UUID idBrand,
         UUID idDistributor,
         String nameProduct,
         String lotProduct,
         LocalDate dataCreatedProduct,
         LocalDate dataDueProduct,
         boolean dataDueProductVerify
        ) {
    public static ProductDueOutput from(Product product){
        return new ProductDueOutput(
                product.getIdProduct().id(),
                product.getIdBrand(),
                product.getIdDistributor(),
                product.getNameProduct(),
                product.getLotProduct(),
                product.getDataCreatedProduct(),
                product.getDataDueProduct(),
                product.isDataDueProductVerify()
        );
    }
}
