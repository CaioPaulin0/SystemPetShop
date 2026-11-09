package com.SystemPetShop;

import com.SystemPetShop.expired.application.ExpiredFindAllUseCase;
import com.SystemPetShop.expired.application.ExpiredVerifyUseCase;
import com.SystemPetShop.expired.domain.Expired;
import com.SystemPetShop.expired.domain.ExpiredRepository;
import com.SystemPetShop.expired.infrastructure.messaging.dto.ExpiredVerifyEvent;
import com.SystemPetShop.expired.infrastructure.messaging.produce.ExpiredVerifyProduce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiredTest {
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BRAND_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DISTRIBUTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 13);
    private static final LocalDate MANUFACTURE = LocalDate.of(2026, 1, 10);
    private static final LocalDate DUE = LocalDate.of(2027, 1, 10);
    @Mock private ExpiredRepository repository;
    @Mock private ExpiredVerifyProduce producer;
    private ExpiredFindAllUseCase findAllDue;
    private ExpiredVerifyUseCase dueVerify;

    @BeforeEach void setUp() {
        findAllDue = new ExpiredFindAllUseCase(repository);
        dueVerify = new ExpiredVerifyUseCase(repository, producer);
    }

    @Test void deveListarProdutosPorValidadePreservandoIdentidadeEDados() {
        UUID outroId = UUID.fromString("00000000-0000-0000-0000-000000000004");
        when(repository.findAllExpiredProduct(PageRequest.of(0, 6))).thenReturn(List.of(produto(ID, true), produto(outroId, false)));
        var result = findAllDue.execute(0);
        assertEquals(List.of(ID, outroId), result.stream().map(p -> p.idProduct()).toList());
        for (var item : result) {
            assertAll(
                    () -> assertEquals(BRAND_ID, item.idBrand()),
                    () -> assertEquals(DISTRIBUTOR_ID, item.idDistributor()),
                    () -> assertEquals("Racao Premium", item.nameProduct()),
                    () -> assertEquals("Marca", item.nameBrand()),
                    () -> assertEquals("Distribuidor", item.nameDistributor()),
                    () -> assertEquals("00123-A", item.lotProduct()),
                    () -> assertEquals(MANUFACTURE, item.dataCreatedProduct()),
                    () -> assertEquals(DUE, item.dataDueProduct()),
                    () -> assertEquals(TODAY, item.dataCreated()),
                    () -> assertEquals(20, item.stockProduct())
            );
        }
        verify(repository).findAllExpiredProduct(PageRequest.of(0, 6));
        verifyNoInteractions(producer);
    }

    @Test void deveRetornarListaVaziaQuandoNaoHaProdutosPorValidade() {
        when(repository.findAllExpiredProduct(PageRequest.of(0, 6))).thenReturn(List.of());
        assertEquals(List.of(), findAllDue.execute(0));
        verify(repository).findAllExpiredProduct(PageRequest.of(0, 6));
    }

    @Test void devePropagarFalhaAoListarProdutosPorValidade() {
        var falha = new RuntimeException("Falha ao consultar validade");
        when(repository.findAllExpiredProduct(PageRequest.of(0, 6))).thenThrow(falha);
        assertSame(falha, assertThrows(RuntimeException.class, () -> findAllDue.execute(0)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 29})
    void deveAvisarQuandoProdutoEstaPertoDoVencimento(int dias) {
        Expired produto = produto(ID, false);
        produto.setDataDueProduct(TODAY.plusDays(dias));
        when(repository.findById(ID)).thenReturn(produto);

        try (MockedStatic<LocalDate> datas = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            datas.when(LocalDate::now).thenReturn(TODAY);
            assertEquals("Produto estar perto do prazo de vencimento, prazo para vencer o produto em "
                    + dias + (dias == 1 ? " dia" : " dias"), dueVerify.execute(ID));
        }

        verify(repository).findById(ID);
        verifyNoMoreInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void deveRegistrarVencimentoQuandoPrazoTerminou(int dias) {
        Expired produto = produto(ID, false);
        produto.setDataDueProduct(TODAY.plusDays(dias));
        when(repository.findById(ID)).thenReturn(produto);
        when(repository.verifyExpiredProduct(ID)).thenReturn("Vencimento registrado");

        try (MockedStatic<LocalDate> datas = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            datas.when(LocalDate::now).thenReturn(TODAY);
            assertEquals("Vencimento registrado", dueVerify.execute(ID));
        }

        verify(repository).verifyExpiredProduct(ID);
        verify(producer).execute(new ExpiredVerifyEvent(ID));
    }

    @ParameterizedTest
    @ValueSource(ints = {30, 31})
    void deveInformarQueProdutoEstaDentroDoPrazo(int dias) {
        Expired produto = produto(ID, true);
        produto.setDataDueProduct(TODAY.plusDays(dias));
        when(repository.findById(ID)).thenReturn(produto);

        try (MockedStatic<LocalDate> datas = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            datas.when(LocalDate::now).thenReturn(TODAY);
            assertEquals("produto dentro do prazo de vencimento", dueVerify.execute(ID));
        }

        verify(repository).findById(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void naoDeveVerificarValidadeComIdNulo() {
        RuntimeException falha = assertThrows(RuntimeException.class, () -> dueVerify.execute(null));

        assertEquals("idProduct is null", falha.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoBuscarProdutoParaVerificarValidade() {
        RuntimeException falha = new RuntimeException("Falha ao buscar produto");
        when(repository.findById(ID)).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> dueVerify.execute(ID)));
        verify(repository, never()).verifyExpiredProduct(any(UUID.class));
    }

    @Test
    void devePropagarFalhaAoRegistrarVencimento() {
        Expired produto = produto(ID, false);
        produto.setDataDueProduct(TODAY);
        RuntimeException falha = new RuntimeException("Falha ao registrar vencimento");
        when(repository.findById(ID)).thenReturn(produto);
        when(repository.verifyExpiredProduct(ID)).thenThrow(falha);

        try (MockedStatic<LocalDate> datas = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            datas.when(LocalDate::now).thenReturn(TODAY);
            assertSame(falha, assertThrows(RuntimeException.class, () -> dueVerify.execute(ID)));
        }
    }

    private Expired produto(UUID id, boolean verificarValidade) {
        return new Expired(BRAND_ID, id, DISTRIBUTOR_ID, verificarValidade, "00123-A",
                "Racao Premium", "Distribuidor", MANUFACTURE, DUE, TODAY, "Marca", 20, true);
    }
}