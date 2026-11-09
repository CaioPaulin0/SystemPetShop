package com.SystemPetShop.distributor.infrastructure.messaging.kafka.repository;

import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DistributorExpiredRepositoryEntity extends JpaRepository<DistributorEntity, UUID> {
}
