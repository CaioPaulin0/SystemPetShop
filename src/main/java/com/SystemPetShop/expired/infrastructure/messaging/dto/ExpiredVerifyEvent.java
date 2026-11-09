package com.SystemPetShop.expired.infrastructure.messaging.dto;

import java.util.UUID;

public record ExpiredVerifyEvent(UUID idProduct) {
    public static ExpiredVerifyEvent toEvent(UUID idProduct){
        return new ExpiredVerifyEvent(idProduct);
    }
}
