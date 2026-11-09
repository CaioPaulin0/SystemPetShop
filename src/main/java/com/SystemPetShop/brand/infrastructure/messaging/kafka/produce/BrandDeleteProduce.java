package com.SystemPetShop.brand.infrastructure.messaging.kafka.produce;

import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandDeleteEvent;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BrandDeleteProduce {

    private final OutboxStore outbox;

    public BrandDeleteProduce(@Qualifier("mainOutbox") OutboxStore outbox) {
        this.outbox = outbox;
    }

    public void execute(BrandDeleteEvent event){

        outbox.enqueue(
                "brand-delete",
                event.idBrand().toString(),
                event
        );
    }
}
