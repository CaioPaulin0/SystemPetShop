package com.SystemPetShop.product.infrastructure.http.response;

import com.SystemPetShop.product.application.output.ProductImageAIOutput;

import java.time.LocalDate;

public record ProductImageAIResponse(
        String nameProduct,
        String typeProduct,
        String descriptionProduct,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        String lotProduct
) {

    public static ProductImageAIResponse from(ProductImageAIOutput productImageAIOutput){
        return new ProductImageAIResponse(
                productImageAIOutput.nameProduct(),
                productImageAIOutput.typeProduct(),
                productImageAIOutput.descriptionProduct(),
                productImageAIOutput.dataCreatedProduct(),
                productImageAIOutput.dataDueProduct(),
                productImageAIOutput.lotProduct()
        );
    }
}
