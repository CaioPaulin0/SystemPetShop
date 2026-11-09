package com.SystemPetShop.product.application.output;

import java.time.LocalDate;

public record ProductImageAIOutput(
        String nameProduct,
        String typeProduct,
        String descriptionProduct,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        String lotProduct
){}
