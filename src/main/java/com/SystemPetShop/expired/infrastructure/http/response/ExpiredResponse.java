package com.SystemPetShop.expired.infrastructure.http.response;

import com.SystemPetShop.expired.application.output.ExpiredOutput;

import java.time.LocalDate;
import java.util.UUID;

public record ExpiredResponse(
        UUID idBrand,
        UUID idProduct,
        UUID idDistributor,
        boolean dataDueProductVerify,
        String lotProduct,
        String nameProduct,
        String nameDistributor,
        LocalDate dataCreatedProduct,
        LocalDate dataDueProduct,
        LocalDate dataCreated,
        String nameBrand,
        long stockProduct
)
{
    public static ExpiredResponse from(ExpiredOutput expired){
        return new ExpiredResponse(
                expired.idBrand(),
                expired.idProduct(),
                expired.idDistributor(),
                expired.dataDueProductVerify(),
                expired.lotProduct(),
                expired.nameProduct(),
                expired.nameDistributor(),
                expired.dataCreatedProduct(),
                expired.dataDueProduct(),
                expired.dataCreated(),
                expired.nameBrand(),
                expired.stockProduct()
        );
    }
}
