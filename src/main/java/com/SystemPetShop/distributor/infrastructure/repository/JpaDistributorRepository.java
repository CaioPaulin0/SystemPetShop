package com.SystemPetShop.distributor.infrastructure.repository;

import com.SystemPetShop.distributor.domain.Distributor;
import com.SystemPetShop.distributor.domain.DistributorRepository;
import com.SystemPetShop.distributor.infrastructure.entity.DistributorEntity;
import com.SystemPetShop.distributor.infrastructure.metrics.DistributorMetricsRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

@Repository
public class JpaDistributorRepository implements DistributorRepository {
    private final DistributorRepositoryEntity distributorRepository;

    private final DistributorMetricsRepository distributorMetricsRepository;

    public JpaDistributorRepository(DistributorRepositoryEntity distributorRepository,
                                    DistributorMetricsRepository distributorMetricsRepository) {
        this.distributorMetricsRepository = distributorMetricsRepository;
        this.distributorRepository = distributorRepository;
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    public List<Distributor> searchMetricsByName(String name, Pageable pageable) {
        return distributorMetricsRepository.findByNameDistributorContainingIgnoreCase(name, pageable).stream()
                .map(DistributorEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    public List<Distributor> searchByName(String name, Pageable pageable) {
        return distributorRepository.findByNameDistributorContainingIgnoreCase(name, pageable).stream()
                .map(DistributorEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Distributor save(Distributor distributor) {
        var entity = DistributorEntity.from(distributor);
        return distributorRepository.save(entity).toDomain();
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public String delete(UUID id) {

        distributorRepository.deleteById(id);
        return "Deleted success";
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public List<Distributor> findAllDistributor(Pageable pageable) {
        var result = StreamSupport.stream(distributorRepository.findAll(pageable).spliterator(), false)
                .map(p -> p.toDomain())
                .toList();

        return result;
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager")
    public List<Distributor> findAllDistributorMetrics(Pageable pageable) {
        return distributorRepository.findAllByOrderByIdDistributorAsc(pageable).stream()
                .map(DistributorEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager")
    public List<Distributor> findAllDistributorMetricsTotal() {

        return distributorRepository.findAll().stream().map(DistributorEntity::toDomain).toList();
    }

    @Override
    @Transactional(transactionManager = "metricsTransactionManager", readOnly = true)
    public List<Double> metricsDistributor(UUID idDistributor) {
        var totals = distributorMetricsRepository.metricsDistributor(idDistributor);
        return List.of(totals.totalBuy(), totals.totalSell());
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public String editDistributor(Distributor distributor) {

        var result = new DistributorEntity(
                distributor.getId().id(),
                distributor.getNameDistributor(),
                distributor.getLocalizationDistributor(),
                distributor.getSalespeopleDistributor(),
                distributor.getTypeDistributor(),
                distributor.getTelephones(),
                distributor.getCNPJ());

        distributorRepository.editDistributor(result);

        return "atualizado";
    }

    @Override
    @Transactional(transactionManager = "mainTransactionManager")
    public Boolean existsById(UUID idDistributor) {
        return distributorRepository.existsById(idDistributor);
    }

}
