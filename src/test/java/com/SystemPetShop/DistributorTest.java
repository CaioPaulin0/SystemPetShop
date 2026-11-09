package com.SystemPetShop;

import com.SystemPetShop.distributor.application.DistributorDeleteUseCase;
import com.SystemPetShop.distributor.application.DistributorEditUseCase;
import com.SystemPetShop.distributor.application.DistributorFindAllUseCase;
import com.SystemPetShop.distributor.application.DistributorMetricsUseCase;
import com.SystemPetShop.distributor.application.DistributorSaveUseCase;
import com.SystemPetShop.distributor.application.input.DistributorEditInput;
import com.SystemPetShop.distributor.application.input.DistributorInput;
import com.SystemPetShop.distributor.application.output.DistributorOutput;
import com.SystemPetShop.distributor.domain.Distributor;
import com.SystemPetShop.distributor.domain.DistributorRepository;
import com.SystemPetShop.distributor.domain.IdDistributor;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce.DistributorProduce;
import com.SystemPetShop.distributor.infrastructure.messaging.kafka.produce.DistributorDeleteProduce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributorTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final List<String> VENDEDORES = List.of("Ana", "Carlos");
    private static final List<String> TELEFONES = List.of("11999990000", "1133334444");

    @Mock
    private DistributorRepository repository;
    @Mock
    private DistributorProduce distributorProduce;
    @Mock
    private DistributorDeleteProduce distributorDeleteProduce;

    private DistributorSaveUseCase save;
    private DistributorEditUseCase edit;
    private DistributorDeleteUseCase delete;
    private DistributorFindAllUseCase findAll;

    private DistributorMetricsUseCase distributorMetrics;

    @BeforeEach
    void setUp() {
        distributorMetrics = new DistributorMetricsUseCase(repository);
        save = new DistributorSaveUseCase(repository, distributorProduce);
        delete = new DistributorDeleteUseCase(repository, distributorDeleteProduce);
        edit = new DistributorEditUseCase(repository);
        findAll = new DistributorFindAllUseCase(repository);
    }

    @Test
    void deveConsultarMetricasDoDistribuidor() {
        List<Double> valores = List.of(300.0, 500.0);
        when(repository.metricsDistributor(ID)).thenReturn(valores);

        assertEquals(valores, distributorMetrics.execute(ID));
        verify(repository).metricsDistributor(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaMetricasDoDistribuidor() {
        when(repository.metricsDistributor(ID)).thenReturn(List.of());

        assertEquals(List.of(), distributorMetrics.execute(ID));
        verify(repository).metricsDistributor(ID);
    }

    @Test
    void devePropagarFalhaAoConsultarMetricasDoDistribuidor() {
        RuntimeException falha = new RuntimeException("Falha nas metricas do distribuidor");
        when(repository.metricsDistributor(ID)).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class,
                () -> distributorMetrics.execute(ID)));
    }

    @Test
    void deveSalvarDistribuidorERetornarDadosPersistidos() {
        when(repository.save(any(Distributor.class))).thenReturn(distribuidor(ID, "Pet Distribuidora"));

        assertEquals(output(ID, "Pet Distribuidora"), save.execute(input()));

        ArgumentCaptor<Distributor> captor = ArgumentCaptor.forClass(Distributor.class);
        verify(repository).save(captor.capture());
        assertNotNull(captor.getValue().getId().id());
        assertDados(captor.getValue());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoSalvarDistribuidor() {
        RuntimeException falha = new RuntimeException("Falha ao salvar");
        when(repository.save(any(Distributor.class))).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> save.execute(input())));
        verify(repository).save(any(Distributor.class));
    }

    @Test
    void deveEditarDistribuidorExistentePreservandoId() throws Exception {
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.editDistributor(any(Distributor.class))).thenReturn("Distribuidor editado");

        assertEquals("Distribuidor editado", edit.execute(editInput()));

        ArgumentCaptor<Distributor> captor = ArgumentCaptor.forClass(Distributor.class);
        verify(repository).existsById(ID);
        verify(repository).editDistributor(captor.capture());
        assertEquals(ID, captor.getValue().getId().id());
        assertDados(captor.getValue());
        verifyNoMoreInteractions(repository);
    }

    @Test
    void naoDeveEditarDistribuidorInexistente() {
        when(repository.existsById(ID)).thenReturn(false);

        Exception falha = assertThrows(Exception.class, () -> edit.execute(editInput()));

        assertEquals("Distributor is null", falha.getMessage());
        verify(repository).existsById(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoEditarDistribuidor() {
        RuntimeException falha = new RuntimeException("Falha ao editar");
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.editDistributor(any(Distributor.class))).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> edit.execute(editInput())));
        verify(repository).editDistributor(any(Distributor.class));
    }

    @Test
    void deveExcluirDistribuidorExistente() throws Exception {
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.delete(ID)).thenReturn("Distribuidor excluido");

        assertEquals("Distribuidor excluido", delete.execute(ID));

        verify(repository).existsById(ID);
        verify(repository).delete(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void naoDeveExcluirDistribuidorInexistente() {
        when(repository.existsById(ID)).thenReturn(false);

        Exception falha = assertThrows(Exception.class, () -> delete.execute(ID));

        assertEquals("Distributor is null", falha.getMessage());
        verify(repository).existsById(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoExcluirDistribuidor() {
        RuntimeException falha = new RuntimeException("Falha ao excluir");
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.delete(ID)).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> delete.execute(ID)));
        verify(repository).delete(ID);
    }

    @Test
    void deveListarDistribuidoresConvertendoTodosOsDados() throws Exception {
        UUID outroId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        when(repository.findAllDistributor( PageRequest.of(1,5))).thenReturn(List.of(
                distribuidor(ID, "Pet Distribuidora"), distribuidor(outroId, "Outra distribuidora")));

//        assertEquals(List.of(output(ID, "Pet Distribuidora"), output(outroId, "Outra distribuidora")),
//                findAll.execute(1);

        verify(repository).findAllDistributor( PageRequest.of(1,5));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deveRejeitarListagemVaziaDeDistribuidores() {
        when(repository.findAllDistributor(PageRequest.of(1,5))).thenReturn(List.of());

        Exception falha = assertThrows(Exception.class, () -> findAll.execute(1));

        assertEquals("list was empty", falha.getMessage());
        verify(repository).findAllDistributor(PageRequest.of(1,5));
    }

    @Test
    void devePropagarFalhaAoListarDistribuidores() {
        RuntimeException falha = new RuntimeException("Falha ao listar");
        when(repository.findAllDistributor( PageRequest.of(1,5))).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> findAll.execute(1)));
        verify(repository).findAllDistributor( PageRequest.of(1,5));
    }

    @Test
    void deveListarPaginaDeDistribuidoresParaMetricas() throws Exception {
        var pagina = PageRequest.of(1, 6);
        when(repository.findAllDistributorMetrics(pagina)).thenReturn(List.of(distribuidor(ID, "Pet Distribuidora")));

        assertEquals(List.of(output(ID, "Pet Distribuidora")), findAll.executeMetrics(1));
        verify(repository).findAllDistributorMetrics(pagina);
    }

    private DistributorInput input() {
        return new DistributorInput("Pet Distribuidora", "Sao Paulo", VENDEDORES,
                "Atacado", TELEFONES, "12345678000190");
    }

    private DistributorEditInput editInput() {
        DistributorInput dados = input();
        return new DistributorEditInput(ID, dados.nameDistributor(), dados.localizationDistributor(),
                dados.salespeopleDistributor(), dados.typeDistributor(), dados.telephones(), dados.CNPJ());
    }

    private Distributor distribuidor(UUID id, String nome) {
        return new Distributor(IdDistributor.from(id), nome, "Sao Paulo", VENDEDORES,
                "Atacado", TELEFONES, "12345678000190");
    }

    private DistributorOutput output(UUID id, String nome) {
        return new DistributorOutput(id.toString(), nome, "Sao Paulo", VENDEDORES,
                "Atacado", TELEFONES, "12345678000190");
    }

    private void assertDados(Distributor distribuidor) {
        assertAll(
                () -> assertEquals("Pet Distribuidora", distribuidor.getNameDistributor()),
                () -> assertEquals("Sao Paulo", distribuidor.getLocalizationDistributor()),
                () -> assertEquals(VENDEDORES, distribuidor.getSalespeopleDistributor()),
                () -> assertEquals("Atacado", distribuidor.getTypeDistributor()),
                () -> assertEquals(TELEFONES, distribuidor.getTelephones()),
                () -> assertEquals("12345678000190", distribuidor.getCNPJ())
        );
    }
}
