package com.SystemPetShop.distributor.infrastructure.repository;

import com.SystemPetShop.distributor.domain.Distributor;
import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.distributor.infrastructure.http.response.DistributorReponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface DistributorRepositoryEntity extends JpaRepository<DistributorEntity, UUID> {

    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    List<DistributorEntity> findByNameDistributorContainingIgnoreCase(String name, Pageable pageable);

    List<DistributorEntity> findAllByOrderByIdDistributorAsc(Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
        UPDATE DistributorEntity distributor
        SET distributor.CNPJ = :#{#distributor.CNPJ},
            distributor.localizationDistributor = :#{#distributor.localizationDistributor},
            distributor.nameDistributor = :#{#distributor.nameDistributor},
            distributor.salespeopleDistributor = :#{#distributor.salespeopleDistributor},
            distributor.telephones = :#{#distributor.telephones},
            distributor.typeDistributor = :#{#distributor.typeDistributor}
        WHERE distributor.idDistributor = :#{#distributor.idDistributor}
        """)
    void editDistributor(@Param("distributor") DistributorEntity distributorEntity);

}
