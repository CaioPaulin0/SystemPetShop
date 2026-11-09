package com.SystemPetShop;

import com.SystemPetShop.brand.application.BrandDeleteUseCase;
import com.SystemPetShop.brand.application.BrandEditUseCase;
import com.SystemPetShop.brand.application.BrandFindAllUseCase;
import com.SystemPetShop.brand.application.BrandMetricsUseCase;
import com.SystemPetShop.brand.application.BrandSaveUseCase;
import com.SystemPetShop.brand.application.input.BrandEditInput;
import com.SystemPetShop.brand.application.input.BrandInput;
import com.SystemPetShop.brand.application.output.BrandOutput;
import com.SystemPetShop.brand.domain.Brand;
import com.SystemPetShop.brand.domain.BrandRepository;
import com.SystemPetShop.brand.domain.IdBrand;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandCreatedEvent;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.dto.BrandDeleteEvent;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandCreateProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandCreateMetricsProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandEditProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandEditMetricsProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandDeleteProduce;
import com.SystemPetShop.brand.infrastructure.messaging.kafka.produce.BrandDeleteMetricsProduce;
import com.SystemPetShop.shared.infrastructure.outbox.OutboxStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private OutboxStore brandOutbox;

    @Mock
    private OutboxStore deleteOutbox;

    private BrandSaveUseCase brandSaveUseCase;
    private BrandEditUseCase brandEditUseCase;
    private BrandDeleteUseCase brandDeleteUseCase;
    private BrandFindAllUseCase brandFindAllUseCase;
    private BrandMetricsUseCase brandMetricsUseCase;

    private final UUID idBrand = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID idDistributor = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void setUp() {
        brandSaveUseCase = new BrandSaveUseCase(brandRepository,
                new BrandCreateProduce(brandOutbox), new BrandCreateMetricsProduce(brandOutbox));
        brandEditUseCase = new BrandEditUseCase(brandRepository,
                new BrandEditProduce(brandOutbox), new BrandEditMetricsProduce(brandOutbox));
        brandDeleteUseCase = new BrandDeleteUseCase(brandRepository,
                new BrandDeleteProduce(deleteOutbox), new BrandDeleteMetricsProduce(deleteOutbox));
        brandMetricsUseCase = new BrandMetricsUseCase(brandRepository);
        brandFindAllUseCase = new BrandFindAllUseCase(brandRepository);
    }

    @AfterEach
    void naoDevePublicarEventosExtrasOuEmCasoDeFalha() {
        verifyNoMoreInteractions(brandOutbox, deleteOutbox);
    }

    @Test
    void deveConsultarMetricasDaMarca() {
        List<Double> valores = List.of(100.0, 250.0);
        when(brandRepository.metricsBrand(idBrand)).thenReturn(valores);

        assertEquals(valores, brandMetricsUseCase.execute(idBrand));
        verify(brandRepository).metricsBrand(idBrand);
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaMetricasDaMarca() {
        when(brandRepository.metricsBrand(idBrand)).thenReturn(List.of());

        assertEquals(List.of(), brandMetricsUseCase.execute(idBrand));
        verify(brandRepository).metricsBrand(idBrand);
    }

    @Test
    void devePropagarFalhaAoConsultarMetricasDaMarca() {
        RuntimeException falha = new RuntimeException("Falha nas metricas da marca");
        when(brandRepository.metricsBrand(idBrand)).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> brandMetricsUseCase.execute(idBrand)));
    }

    @Test
    void deveSalvarMarcaERetornarDadosPersistidos() {
        BrandInput input = new BrandInput(idDistributor, "Pet Food", "Racao");
        Brand marcaSalva = new Brand(IdBrand.from(idBrand), idDistributor, "Pet Food", "Racao");
        when(brandRepository.save(any(Brand.class))).thenReturn(marcaSalva);

        BrandOutput resultado = brandSaveUseCase.execute(input);

        assertEquals(new BrandOutput(idBrand, idDistributor, "Pet Food", "Racao"), resultado);
        var event = new BrandCreatedEvent(idBrand, idDistributor, "Pet Food", "Racao");
        var order = inOrder(brandRepository, brandOutbox);
        order.verify(brandRepository).save(any(Brand.class));
        order.verify(brandOutbox).enqueue("brand-created", idBrand.toString(), event);
        order.verify(brandOutbox).enqueue("brand-created-metrics", idBrand.toString(), event);
        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(brandRepository).save(captor.capture());
        Brand marcaEnviada = captor.getValue();
        assertAll(
                () -> assertNotNull(marcaEnviada.getIdBrand()),
                () -> assertNotNull(marcaEnviada.getIdBrand().id()),
                () -> assertEquals(idDistributor, marcaEnviada.getIdDistributor()),
                () -> assertEquals("Pet Food", marcaEnviada.getNameBrand()),
                () -> assertEquals("Racao", marcaEnviada.getTypeBrand())
        );
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void devePropagarFalhaAoSalvarMarca() {
        BrandInput input = new BrandInput(idDistributor, "Pet Food", "Racao");
        RuntimeException falha = new RuntimeException("Falha ao salvar marca");
        when(brandRepository.save(any(Brand.class))).thenThrow(falha);

        RuntimeException resultado = assertThrows(RuntimeException.class,
                () -> brandSaveUseCase.execute(input));

        assertSame(falha, resultado);
        verify(brandRepository).save(any(Brand.class));
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void deveEditarMarcaExistentePreservandoIdentificador() throws Exception {
        BrandEditInput input = new BrandEditInput(idBrand, idDistributor, "Nova marca", "Higiene");
        when(brandRepository.existsById(idBrand)).thenReturn(true);
        when(brandRepository.editBrand(any(Brand.class))).thenReturn("Marca editada");

        String resultado = brandEditUseCase.execute(input);

        assertEquals("Marca editada", resultado);
        var event = new BrandCreatedEvent(idBrand, idDistributor, "Nova marca", "Higiene");
        var order = inOrder(brandRepository, brandOutbox);
        order.verify(brandRepository).editBrand(any(Brand.class));
        order.verify(brandOutbox).enqueue("brand-update", idBrand.toString(), event);
        order.verify(brandOutbox).enqueue("brand-update-metrics", idBrand.toString(), event);
        ArgumentCaptor<Brand> captor = ArgumentCaptor.forClass(Brand.class);
        verify(brandRepository).existsById(idBrand);
        verify(brandRepository).editBrand(captor.capture());
        Brand marcaEnviada = captor.getValue();
        assertAll(
                () -> assertEquals(idBrand, marcaEnviada.getIdBrand().id()),
                () -> assertEquals(idDistributor, marcaEnviada.getIdDistributor()),
                () -> assertEquals("Nova marca", marcaEnviada.getNameBrand()),
                () -> assertEquals("Higiene", marcaEnviada.getTypeBrand())
        );
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void naoDeveEditarMarcaInexistente() {
        BrandEditInput input = new BrandEditInput(idBrand, idDistributor, "Nova marca", "Higiene");
        when(brandRepository.existsById(idBrand)).thenReturn(false);

        Exception resultado = assertThrows(Exception.class, () -> brandEditUseCase.execute(input));

        assertEquals("Brand is null", resultado.getMessage());
        verify(brandRepository).existsById(idBrand);
        verify(brandRepository, never()).editBrand(any(Brand.class));
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void devePropagarFalhaAoEditarMarca() {
        BrandEditInput input = new BrandEditInput(idBrand, idDistributor, "Nova marca", "Higiene");
        RuntimeException falha = new RuntimeException("Falha ao editar marca");
        when(brandRepository.existsById(idBrand)).thenReturn(true);
        when(brandRepository.editBrand(any(Brand.class))).thenThrow(falha);

        RuntimeException resultado = assertThrows(RuntimeException.class,
                () -> brandEditUseCase.execute(input));

        assertSame(falha, resultado);
        verify(brandRepository).existsById(idBrand);
        verify(brandRepository).editBrand(any(Brand.class));
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void deveExcluirMarcaExistente() throws Exception {
        when(brandRepository.existsById(idBrand)).thenReturn(true);
        when(brandRepository.deletBrand(idBrand)).thenReturn("Marca excluida");

        String resultado = brandDeleteUseCase.execute(idBrand);

        assertEquals("Marca excluida", resultado);
        var event = new BrandDeleteEvent(idBrand);
        var order = inOrder(brandRepository, deleteOutbox);
        order.verify(brandRepository).deletBrand(idBrand);
        order.verify(deleteOutbox).enqueue("brand-delete", idBrand.toString(), event);
        order.verify(deleteOutbox).enqueue("brand-delete-metrics", idBrand.toString(), event);
        verify(brandRepository).existsById(idBrand);
        verify(brandRepository).deletBrand(idBrand);
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void naoDeveExcluirMarcaInexistente() {
        when(brandRepository.existsById(idBrand)).thenReturn(false);

        Exception resultado = assertThrows(Exception.class, () -> brandDeleteUseCase.execute(idBrand));

        assertEquals("Brand is null", resultado.getMessage());
        verify(brandRepository).existsById(idBrand);
        verify(brandRepository, never()).deletBrand(any(UUID.class));
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void devePropagarFalhaAoExcluirMarca() {
        RuntimeException falha = new RuntimeException("Falha ao excluir marca");
        when(brandRepository.existsById(idBrand)).thenReturn(true);
        when(brandRepository.deletBrand(idBrand)).thenThrow(falha);

        RuntimeException resultado = assertThrows(RuntimeException.class,
                () -> brandDeleteUseCase.execute(idBrand));

        assertSame(falha, resultado);
        verify(brandRepository).existsById(idBrand);
        verify(brandRepository).deletBrand(idBrand);
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void deveListarMarcasDoDistribuidorConvertendoTodosOsDados() throws Exception {
        UUID idOutraMarca = UUID.fromString("00000000-0000-0000-0000-000000000003");
        Brand primeiraMarca = new Brand(IdBrand.from(idBrand), idDistributor, "Pet Food", "Racao");
        Brand segundaMarca = new Brand(IdBrand.from(idOutraMarca), idDistributor, "Pet Clean", "Higiene");
        when(brandRepository.findAllByDistributor(idDistributor, PageRequest.of(1, 6)))
                .thenReturn(List.of(primeiraMarca, segundaMarca));

        List<BrandOutput> resultado = brandFindAllUseCase.execute(idDistributor, 1);

        assertEquals(List.of(
                new BrandOutput(idBrand, idDistributor, "Pet Food", "Racao"),
                new BrandOutput(idOutraMarca, idDistributor, "Pet Clean", "Higiene")
        ), resultado);
        verify(brandRepository).findAllByDistributor(idDistributor, PageRequest.of(1, 6));
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void deveRetornarListaVaziaQuandoDistribuidorNaoPossuiMarcas() throws Exception {
        when(brandRepository.findAllByDistributor(idDistributor, PageRequest.of(0, 6))).thenReturn(List.of());

        List<BrandOutput> resultado = brandFindAllUseCase.execute(idDistributor, 0);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(brandRepository).findAllByDistributor(idDistributor, PageRequest.of(0, 6));
        verifyNoMoreInteractions(brandRepository);
    }

    @Test
    void naoDeveListarMarcasComIdentificadorNulo() {
        Exception resultado = assertThrows(Exception.class, () -> brandFindAllUseCase.execute(null, 0));

        assertEquals("brand is null", resultado.getMessage());
        verifyNoInteractions(brandRepository);
    }

    @Test
    void devePropagarFalhaAoListarMarcas() {
        RuntimeException falha = new RuntimeException("Falha ao listar marcas");
        when(brandRepository.findAllByDistributor(idDistributor, PageRequest.of(0, 6))).thenThrow(falha);

        RuntimeException resultado = assertThrows(RuntimeException.class,
                () -> brandFindAllUseCase.execute(idDistributor, 0));

        assertSame(falha, resultado);
        verify(brandRepository).findAllByDistributor(idDistributor, PageRequest.of(0, 6));
        verifyNoMoreInteractions(brandRepository);
    }
}
