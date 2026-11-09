package com.SystemPetShop.brand.infrastructure.http.dto;

import java.util.UUID;

public record BrandFindAllByDistributorDto(
        UUID idDistributor
) {}
