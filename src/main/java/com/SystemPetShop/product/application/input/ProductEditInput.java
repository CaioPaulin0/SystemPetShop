package com.SystemPetShop.product.application.input;

import java.time.LocalDate;
import java.util.UUID;

public record ProductEditInput(
        UUID idProduct,
        UUID idBrand,
        UUID idDistributor,
        String typeProduct,
        String descriptionProduct,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        String lotProduct,
        double priceBuy,
        double priceSell,
        long stockProduct
) {
}
