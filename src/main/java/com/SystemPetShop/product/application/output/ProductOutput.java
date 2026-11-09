package com.SystemPetShop.product.application.output;

import com.SystemPetShop.product.domain.Product;
import java.time.LocalDate;
import java.util.UUID;

public record ProductOutput(
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
) {
    public static ProductOutput from(Product product){
        return new ProductOutput(
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
                product.getVersionProduct()
        );
    }

}
