package com.SystemPetShop;

import com.SystemPetShop.product.application.ProductDeleteUseCase;
import com.SystemPetShop.product.application.ProductEditUseCase;
import com.SystemPetShop.product.application.ProductNewVersionUseCase;
import com.SystemPetShop.product.application.ProductSaveUseCase;
import com.SystemPetShop.product.application.input.ProductEditInput;
import com.SystemPetShop.product.application.input.ProductInput;
import com.SystemPetShop.product.application.input.ProductNewVersionInput;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductCreatedEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductDeleteEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.dto.ProductNewVersionMetricsEvent;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductMetricsMessagingTest {
    @Mock private ProductRepository repository;
    @Mock private OutboxStore productOutbox;
    @Mock private OutboxStore deleteOutbox;
    @Mock private OutboxStore versionOutbox;
    @Mock private OutboxStore versionMetricsOutbox;

    private ProductSaveUseCase save;
    private ProductEditUseCase edit;
    private ProductDeleteUseCase delete;
    private ProductNewVersionUseCase newVersion;

    private final UUID brandId = UUID.randomUUID();
    private final UUID distributorId = UUID.randomUUID();
    private final LocalDate date = LocalDate.of(2026, 11, 3);

    @BeforeEach
    void setUp() {
        save = new ProductSaveUseCase(repository, new ProductCreatedProduce(productOutbox),
                new ProductCreatedMetricsProduce(productOutbox));
        edit = new ProductEditUseCase(repository, new ProductEditProduce(productOutbox),
                new ProductEditMetricsProduce(productOutbox));
        delete = new ProductDeleteUseCase(repository, new ProductDeleteProduce(deleteOutbox),
                new ProductDeleteMetricsProduce(deleteOutbox));
        newVersion = new ProductNewVersionUseCase(repository, new ProductNewVersionProduce(versionOutbox),
                new ProductNewVersionMetricsProduce(versionMetricsOutbox));
    }

    @AfterEach
    void naoDevePublicarEventosExtrasOuAposFalha() {
        verifyNoMoreInteractions(productOutbox, deleteOutbox, versionOutbox, versionMetricsOutbox);
    }

    @Test
    void devePublicarProdutoPersistidoNosTopicosExpiredEMetrics() {
        var product = product();
        when(repository.save(any(Product.class))).thenReturn(product);

        var result = save.execute(input());
        var event = ProductCreatedEvent.toEvent(product);
        assertEquals(product.getIdProduct().id(), result.idProduct());
        var order = inOrder(repository, productOutbox);
        order.verify(repository).save(any(Product.class));
        order.verify(productOutbox).enqueue("product-created", result.idProduct().toString(), event);
        order.verify(productOutbox).enqueue("product-created-metrics", result.idProduct().toString(), event);
    }

    @Test
    void naoDevePublicarQuandoSalvarFalhar() {
        when(repository.save(any(Product.class))).thenThrow(new IllegalStateException("save failed"));
        assertThrows(IllegalStateException.class, () -> save.execute(input()));
    }

    @Test
    void deveEditarEPublicarPrecosDeCompraEVendaCorretos() throws Exception {
        var product = product();
        var id = product.getIdProduct().id();
        when(repository.existsById(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(product);
        when(repository.editProduct(product)).thenReturn("edited");

        assertEquals("edited", edit.execute(editInput(id)));
        assertEquals(25, product.getPriceBuy());
        assertEquals(40, product.getPriceSell());
        assertEquals("00AB-002", product.getLotProduct());
        var event = ProductCreatedEvent.toEvent(product);
        var order = inOrder(repository, productOutbox);
        order.verify(repository).editProduct(product);
        order.verify(productOutbox).enqueue("product-update", id.toString(), event);
        order.verify(productOutbox).enqueue("product-update-metrics", id.toString(), event);
    }

    @Test
    void naoDevePublicarEdicaoDeProdutoInexistente() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);
        assertThrows(Exception.class, () -> edit.execute(editInput(id)));
        verify(repository, never()).editProduct(any());
    }

    @Test
    void naoDevePublicarQuandoEdicaoFalhar() {
        var product = product();
        var id = product.getIdProduct().id();
        when(repository.existsById(id)).thenReturn(true);
        when(repository.findById(id)).thenReturn(product);
        when(repository.editProduct(product)).thenThrow(new IllegalStateException("edit failed"));
        assertThrows(IllegalStateException.class, () -> edit.execute(editInput(id)));
    }

    @Test
    void deveExcluirAntesDePublicarNosDoisTopicos() throws Exception {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        assertEquals("Product Deleted", delete.execute(id));
        var event = new ProductDeleteEvent(id);
        var order = inOrder(repository, deleteOutbox);
        order.verify(repository).delete(id);
        order.verify(deleteOutbox).enqueue("product-delete", id.toString(), event);
        order.verify(deleteOutbox).enqueue("product-delete-metrics", id.toString(), event);
    }

    @Test
    void naoDevePublicarExclusaoDeProdutoInexistente() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);
        assertThrows(Exception.class, () -> delete.execute(id));
        verify(repository, never()).delete(any());
    }

    @Test
    void naoDevePublicarQuandoExclusaoFalhar() {
        var id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        when(repository.delete(id)).thenThrow(new IllegalStateException("delete failed"));
        assertThrows(IllegalStateException.class, () -> delete.execute(id));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deveSemprePublicarNovaVersaoEmMetricsComIdentificadorAnterior(boolean lotExists) {
        var previousId = UUID.randomUUID();
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.existLot(any(Product.class))).thenReturn(lotExists);

        var result = newVersion.execute(versionInput(previousId));

        assertNotEquals(previousId, result.idProduct());
        assertEquals(2, result.versionProduct());
        var captor = org.mockito.ArgumentCaptor.forClass(Product.class);
        var order = inOrder(repository, versionMetricsOutbox);
        order.verify(repository).changeValueProductEnable(previousId);
        order.verify(repository).save(captor.capture());
        var event = ProductNewVersionMetricsEvent.toEvent(previousId, captor.getValue());
        order.verify(versionMetricsOutbox).enqueue("product-new-version-metrics", result.idProduct().toString(), event);
        if (lotExists) {
            verify(versionOutbox).enqueue("product-new-version", result.idProduct().toString(), event.product());
        }
    }

    @Test
    void naoDevePublicarNovaVersaoQuandoSalvarFalhar() {
        when(repository.save(any(Product.class))).thenThrow(new IllegalStateException("save failed"));
        assertThrows(IllegalStateException.class, () -> newVersion.execute(versionInput(UUID.randomUUID())));
    }

    @Test
    void naoDevePublicarNovaVersaoQuandoDesativacaoFalhar() {
        var id = UUID.randomUUID();
        when(repository.changeValueProductEnable(id)).thenThrow(new IllegalStateException("disable failed"));
        assertThrows(RuntimeException.class, () -> newVersion.execute(versionInput(id)));
        verify(repository, never()).save(any());
    }

    private Product product() {
        return new Product(brandId, distributorId, "Racao", "Alimento", "Descricao", date,
                date.plusYears(1), "00123", 10, 20, 5, false, 1, true);
    }

    private ProductInput input() {
        return new ProductInput(brandId, distributorId, "Racao", "Alimento", "Descricao", date,
                date.plusYears(1), "00123", 10, 20, 5, false, 1);
    }

    private ProductEditInput editInput(UUID id) {
        return new ProductEditInput(id, brandId, distributorId, "Higiene", "Editado", date,
                date.plusYears(2), "00AB-002", 25, 40, 8);
    }

    private ProductNewVersionInput versionInput(UUID id) {
        return new ProductNewVersionInput(id, brandId, distributorId, "Racao nova", "Alimento", "Descricao",
                date, date.plusYears(1), "00234", 15, 30, 7, false, 1);
    }
}
