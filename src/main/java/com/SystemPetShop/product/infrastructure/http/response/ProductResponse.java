package com.SystemPetShop.product.infrastructure.http.response;

import com.SystemPetShop.product.application.output.ProductOutput;

import java.time.LocalDate;
import java.util.UUID;

public record ProductResponse(
        UUID idProduct,
        UUID idBrand,
        UUID idDistributor,
        String nameProduct,
        String typeProduct,
        String descriptionProduct,
        LocalDate dataCreated,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        String lotProduct,
        double priceBuy,
        double priceSell,
        long stockProduct,
        boolean dataDueProductVerify,
        long versionProduct
        )
{
    public static ProductResponse from(ProductOutput productOutput){
       return new ProductResponse(
                productOutput.idProduct(),
                productOutput.idBrand(),
                productOutput.idDistributor(),
                productOutput.nameProduct(),
                productOutput.typeProduct(),
                productOutput.descriptionProduct(),
                productOutput.dataCreated(),
                productOutput.dataCreatedProduct(),
                productOutput.dataDueProduct(),
                productOutput.lotProduct(),
                productOutput.priceBuy(),
                productOutput.priceSell(),
                productOutput.stockProduct(),
                productOutput.dataDueProductVerify(),
                productOutput.versionProduct()
        );
    }
}
