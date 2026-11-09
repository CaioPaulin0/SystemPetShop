package com.SystemPetShop.brand.application.input;

import java.util.UUID;

public record BrandEditInput(
        UUID idBrand,
        UUID idDistributor,
        String nameBrand,
        String typeBrand
) {
}
