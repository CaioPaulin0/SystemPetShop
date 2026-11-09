package com.SystemPetShop.expired.application.output;

import com.SystemPetShop.expired.domain.Expired;

import java.time.LocalDate;
import java.util.UUID;

public record ExpiredOutput(
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
        ){

    public static ExpiredOutput from(Expired expired){
        return new ExpiredOutput(
                expired.getIdBrand(),
                expired.getIdProduct(),
                expired.getIdDistributor(),
                expired.isDataDueProductVerify(),
                expired.getLotProduct(),
                expired.getNameProduct(),
                expired.getNameDistributor(),
                expired.getDataCreatedProduct(),
                expired.getDataDueProduct(),
                expired.getDataCreated(),
                expired.getNameBrand(),
                expired.getStockProduct()
        );
    }

}
