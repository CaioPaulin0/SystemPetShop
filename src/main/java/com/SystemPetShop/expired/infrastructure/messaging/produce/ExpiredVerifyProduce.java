package com.SystemPetShop.expired.infrastructure.messaging.produce;

import com.SystemPetShop.expired.infrastructure.messaging.dto.ExpiredVerifyEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ExpiredVerifyProduce {
    private final OutboxStore outbox;

    public ExpiredVerifyProduce(@Qualifier("expiredOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void execute(ExpiredVerifyEvent event){
        outbox.enqueue(
                "expired-verify",
                event.idProduct().toString(),
                event
        );
    }
}
