package com.SystemPetShop.product.infrastructure.http.response;

import com.SystemPetShop.product.application.output.ProductDueOutput;

import java.time.LocalDate;
import java.util.UUID;

public record ProductDueResponse(
        UUID idProduct,
        UUID idBrand,
        UUID idDistributor,
        String nameProduct,
        String lotProduct,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        boolean dataDueProductVerify
) {

    public static ProductDueResponse from(ProductDueOutput productDueOutput){
        return new ProductDueResponse(
                productDueOutput.idProduct(),
                productDueOutput.idBrand(),
                productDueOutput.idDistributor(),
                productDueOutput.nameProduct(),
                productDueOutput.lotProduct(),
                productDueOutput.dataCreatedProduct(),
                productDueOutput.dataDueProduct(),
                productDueOutput.dataDueProductVerify()
        );
    }
}
