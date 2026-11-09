package com.SystemPetShop.expired.domain;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ExpiredRepository {
    List<Expired> filterByDataDueProductVerify(boolean dataDueProductVerify, Pageable pageable);
    List<Expired> searchByName(String name, Pageable pageable);
    Expired findById(UUID idProduct);
    List<Expired> findAllExpiredProduct(Pageable pageable);
    String verifyExpiredProduct(UUID idProduct);
}
