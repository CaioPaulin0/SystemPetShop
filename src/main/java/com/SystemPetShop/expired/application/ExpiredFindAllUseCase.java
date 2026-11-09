package com.SystemPetShop.expired.application;

import com.SystemPetShop.expired.application.output.ExpiredOutput;
import com.SystemPetShop.expired.domain.ExpiredRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpiredFindAllUseCase {

    private final ExpiredRepository expiredRepository;

    public ExpiredFindAllUseCase(ExpiredRepository expiredRepository) {
        this.expiredRepository = expiredRepository;
    }

    public List<ExpiredOutput> execute(int page) {
        return expiredRepository.findAllExpiredProduct(PageRequest.of(page, 8)).stream()
                .map(ExpiredOutput::from)
                .toList();
    }
}
