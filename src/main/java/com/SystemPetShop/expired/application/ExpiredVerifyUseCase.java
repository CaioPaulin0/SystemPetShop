package com.SystemPetShop.expired.application;

import com.SystemPetShop.expired.domain.Expired;
import com.SystemPetShop.expired.domain.ExpiredRepository;
import com.SystemPetShop.expired.infrastructure.messaging.dto.ExpiredVerifyEvent;
import com.SystemPetShop.expired.infrastructure.messaging.produce.ExpiredVerifyProduce;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ExpiredVerifyUseCase {

    private final ExpiredRepository expiredRepository;
    private final ExpiredVerifyProduce expiredVerifyProduce;

    public ExpiredVerifyUseCase(ExpiredRepository expiredRepository, ExpiredVerifyProduce expiredVerifyProduce) {
        this.expiredRepository = expiredRepository;
        this.expiredVerifyProduce = expiredVerifyProduce;
    }

    @Transactional(transactionManager = "expiredTransactionManager", rollbackFor = Exception.class)
    public String execute(UUID idProduct){
        if(idProduct == null){
            throw new RuntimeException("idProduct is null");
        }

        Expired result = expiredRepository.findById(idProduct);
        LocalDate today = LocalDate.now();
        LocalDate dueDate = result.getDataDueProduct();
        long totalDays = ChronoUnit.DAYS.between(today, dueDate);
        if(totalDays >= 1 && totalDays <= 29){
            if(totalDays == 1){
                return "Produto estar perto do prazo de vencimento, prazo para vencer o produto em " + totalDays + " dia";
            }
            return "Produto estar perto do prazo de vencimento, prazo para vencer o produto em " + totalDays + " dias";
        }

        if(totalDays <= 0){
            expiredVerifyProduce.execute(ExpiredVerifyEvent.toEvent(idProduct));
            return expiredRepository.verifyExpiredProduct(idProduct);
        }

        if(totalDays < 30){
            return "produto esta vencido";
        }

        return "produto dentro do prazo de vencimento";
    }
}
