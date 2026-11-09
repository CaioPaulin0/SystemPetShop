package com.SystemPetShop.product.application.input;

import java.time.LocalDate;
import java.util.UUID;

public record ProductNewVersionInput(
        UUID idProduct,
        UUID idBrand,
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
        long versionProduct
) {
}
