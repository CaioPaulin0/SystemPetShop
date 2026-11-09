package com.SystemPetShop.brand.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BrandEditProduce  {
    private final OutboxStore outbox;

    public BrandEditProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void execute(BrandCreatedEvent event){
        outbox.enqueue(
                "brand-update",
                event.idBrand().toString(),
                event
        );
    }
}
