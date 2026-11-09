package com.SystemPetShop.expired.infrastructure.repository;

import com.SystemPetShop.expired.domain.Expired;
import com.SystemPetShop.expired.domain.ExpiredRepository;
import com.SystemPetShop.expired.infrastructure.entity.ExpiredEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@Transactional(transactionManager = "expiredTransactionManager")
public class JpaExpiredEntityRepository implements ExpiredRepository {

    private final ExpiredEntityRepository expiredEntityRepository;

    public JpaExpiredEntityRepository(ExpiredEntityRepository expiredEntityRepository) {
        this.expiredEntityRepository = expiredEntityRepository;
    }

    @Override
    @Transactional(transactionManager = "expiredTransactionManager", readOnly = true)
    public List<Expired> filterByDataDueProductVerify(boolean dataDueProductVerify, Pageable pageable) {
        return expiredEntityRepository.filterByDataDueProductVerify(dataDueProductVerify, pageable).stream()
                .map(result -> ExpiredEntity.fromProduct(
                        result.getProduct(), result.getNameBrand(), result.getNameDistributor()).toDomain())
                .toList();
    }

    @Override
    @Transactional(transactionManager = "expiredTransactionManager", readOnly = true)
    public List<Expired> searchByName(String name, Pageable pageable) {
        String escapedName = name.replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return expiredEntityRepository.searchByName(escapedName, pageable).stream()
                .map(result -> ExpiredEntity.fromProduct(
                        result.getProduct(), result.getNameBrand(), result.getNameDistributor()).toDomain())
                .toList();
    }

    @Override
    public Expired findById(UUID idProduct) {
        var result = expiredEntityRepository.findExpiredByIdProduct(idProduct)
                .orElseThrow(() -> new RuntimeException("product not found"));
        return ExpiredEntity.fromProduct(
                result.getProduct(), result.getNameBrand(), result.getNameDistributor()).toDomain();
    }

    @Override
    public List<Expired> findAllExpiredProduct(Pageable pageable) {
        return expiredEntityRepository.findAllExpiredProduct(pageable).stream()
                .map(result -> ExpiredEntity.fromProduct(
                        result.getProduct(), result.getNameBrand(), result.getNameDistributor()))
                .map(ExpiredEntity::toDomain)
                .toList();
    }

    @Override
    public String verifyExpiredProduct(UUID idProduct) {
        expiredEntityRepository.verifyDueProduct(idProduct);

        return "produto vencido";
    }
}
