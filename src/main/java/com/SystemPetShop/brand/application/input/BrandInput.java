package com.SystemPetShop.brand.application.input;

import java.util.UUID;

public record BrandInput(
        UUID idDistributor,
        String nameBrand,
        String typeBrand
) {}
