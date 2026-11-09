package com.SystemPetShop;

import com.SystemPetShop.shared.infrastructure.outbox.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.mockito.Mockito.*;

class OutboxPublisherTest {
    @Test void limitaLoteEProcessaAmbosOsBancos() {
        var main = mock(OutboxStore.class);
        var expired = mock(OutboxStore.class);
        var sender = mock(OutboxSender.class);
        when(main.publishNext(sender)).thenReturn(true);
        when(expired.publishNext(sender)).thenReturn(true, false);
        new OutboxPublisher(List.of(main, expired), sender, 3).publishPending();
        verify(main, times(3)).publishNext(sender);
        verify(expired, times(2)).publishNext(sender);
    }

    @Test void falhaEmUmBancoNaoInterrompeOutroENemProximaExecucao() {
        var main = mock(OutboxStore.class);
        var expired = mock(OutboxStore.class);
        var sender = mock(OutboxSender.class);
        when(main.publishNext(sender)).thenThrow(new IllegalStateException("Database unavailable")).thenReturn(false);
        var publisher = new OutboxPublisher(List.of(main, expired), sender, 3);
        publisher.publishPending();
        publisher.publishPending();
        verify(main, times(2)).publishNext(sender);
        verify(expired, times(2)).publishNext(sender);
    }
}
