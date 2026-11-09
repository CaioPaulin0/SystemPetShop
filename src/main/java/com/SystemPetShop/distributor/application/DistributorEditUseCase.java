package com.SystemPetShop.distributor.application;

import com.SystemPetShop.distributor.application.input.DistributorEditInput;
import com.SystemPetShop.distributor.application.input.DistributorInput;
import com.SystemPetShop.distributor.application.output.DistributorOutput;
import com.SystemPetShop.distributor.domain.Distributor;
import com.SystemPetShop.distributor.domain.DistributorRepository;
import com.SystemPetShop.distributor.domain.IdDistributor;
import com.SystemPetShop.product.domain.Product;
import org.springframework.stereotype.Service;

@Service
public class DistributorEditUseCase {
    private final DistributorRepository distributorRepository;

    public DistributorEditUseCase(DistributorRepository distributorRepository) {
        this.distributorRepository = distributorRepository;
    }

    public String execute(DistributorEditInput distributorEditInput) throws Exception {

        if(!distributorRepository.existsById(distributorEditInput.idDistributor())){
            throw new Exception("Distributor is null");
        }

                Distributor entity = new Distributor(
                        distributorEditInput.CNPJ(),
                        distributorEditInput.typeDistributor(),
                        distributorEditInput.nameDistributor(),
                        distributorEditInput.localizationDistributor(),
                        distributorEditInput.telephones(),
                        distributorEditInput.salespeopleDistributor()
                );

        entity.setId(IdDistributor.from(distributorEditInput.idDistributor()));

        return distributorRepository.editDistributor(entity);

    }
}