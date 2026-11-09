package com.SystemPetShop.expired.application;

import com.SystemPetShop.expired.application.output.ExpiredOutput;
import com.SystemPetShop.expired.domain.ExpiredRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpiredFilterUseCase {
    private final ExpiredRepository expiredRepository;

    public ExpiredFilterUseCase(ExpiredRepository expiredRepository) {
        this.expiredRepository = expiredRepository;
    }

    public List<ExpiredOutput> execute(boolean dataDueProductVerify, int page) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        return expiredRepository.filterByDataDueProductVerify(dataDueProductVerify,
                        PageRequest.of(page, 8, Sort.by("nameProduct", "idProduct"))).stream()
                .map(ExpiredOutput::from)
                .toList();
    }
}
