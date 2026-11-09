package com.SystemPetShop.expired.application;

import com.SystemPetShop.expired.application.output.ExpiredOutput;
import com.SystemPetShop.expired.domain.ExpiredRepository;
import com.SystemPetShop.shared.application.NameSearch;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpiredSearchUseCase {
    private final ExpiredRepository expiredRepository;

    public ExpiredSearchUseCase(ExpiredRepository expiredRepository) {
        this.expiredRepository = expiredRepository;
    }

    public List<ExpiredOutput> execute(String name, int page) {
        return NameSearch.execute(name, page, Sort.by("nameProduct", "idProduct"),
                expiredRepository::searchByName, ExpiredOutput::from);
    }
}
