package com.SystemPetShop;

import com.SystemPetShop.product.application.ProductDeleteUseCase;
import com.SystemPetShop.product.application.ProductEditUseCase;
import com.SystemPetShop.product.application.ProductFindAllUseCase;
import com.SystemPetShop.product.application.ProductImageAIUseCase;
import com.SystemPetShop.product.application.ProductMetricUseCase;
import com.SystemPetShop.product.application.ProductNewVersionUseCase;
import com.SystemPetShop.product.application.ProductSaveUseCase;
import com.SystemPetShop.product.application.input.ProductEditInput;
import com.SystemPetShop.product.application.input.ProductInput;
import com.SystemPetShop.product.application.input.ProductNewVersionInput;
import com.SystemPetShop.product.application.output.ProductImageAIOutput;
import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.IdProduct;
import com.SystemPetShop.product.domain.Product;
import com.SystemPetShop.product.domain.ProductRepository;
import com.SystemPetShop.product.infrastructure.messaging.kafka.produce.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BRAND_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID DISTRIBUTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 13);
    private static final LocalDate MANUFACTURE = LocalDate.of(2026, 1, 10);
    private static final LocalDate DUE = LocalDate.of(2027, 1, 10);

    @Mock
    private ProductRepository repository;
    @Mock
    private ChatModel chatModel;
    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private ChatClient chatClient;
    @Mock
    private ChatClient.ChatClientRequestSpec request;
    @Mock
    private ChatClient.CallResponseSpec response;
    @Mock private ProductCreatedProduce createdProduce;
    @Mock private ProductCreatedMetricsProduce createdMetricsProduce;
    @Mock private ProductEditProduce editProduce;
    @Mock private ProductEditMetricsProduce editMetricsProduce;
    @Mock private ProductDeleteProduce deleteProduce;
    @Mock private ProductDeleteMetricsProduce deleteMetricsProduce;
    @Mock private ProductNewVersionProduce newVersionProduce;
    @Mock private ProductNewVersionMetricsProduce newVersionMetricsProduce;

    private ProductSaveUseCase save;
    private ProductEditUseCase edit;
    private ProductDeleteUseCase delete;
    private ProductFindAllUseCase findAll;
    private ProductNewVersionUseCase newVersion;
    private ProductMetricUseCase metrics;

    @BeforeEach
    void setUp() {
        save = new ProductSaveUseCase(repository, createdProduce, createdMetricsProduce);
        edit = new ProductEditUseCase(repository, editProduce, editMetricsProduce);
        delete = new ProductDeleteUseCase(repository, deleteProduce, deleteMetricsProduce);
        findAll = new ProductFindAllUseCase(repository);
        newVersion = new ProductNewVersionUseCase(repository, newVersionProduce, newVersionMetricsProduce);
        metrics = new ProductMetricUseCase(repository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void deveSalvarProdutoPreservandoDadosEIndicadorDeValidade(boolean verificarValidade) {
        Product persistido = produto(ID, verificarValidade);
        when(repository.save(any(Product.class))).thenReturn(persistido);

        ProductOutput resultado = save.execute(input(verificarValidade));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        Product enviado = captor.getValue();
        assertNotNull(enviado.getIdProduct().id());
        assertNotNull(enviado.getDataCreated());
        assertDadosProduto(enviado, verificarValidade, 2);
        assertEquals(output(ID, verificarValidade, 2), resultado);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoSalvarProduto() {
        RuntimeException falha = new RuntimeException("Falha ao salvar");
        when(repository.save(any(Product.class))).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> save.execute(input(false))));
        verify(repository).save(any(Product.class));
    }

    @Test
    void deveEditarProdutoExistentePreservandoIdentidadeEAtualizandoCampos() throws Exception {
        Product existente = produto(ID, true);
        ProductEditInput dados = editInput();
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.findById(ID)).thenReturn(existente);
        when(repository.editProduct(existente)).thenReturn("Produto editado");

        assertEquals("Produto editado", edit.execute(dados));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).editProduct(captor.capture());
        Product enviado = captor.getValue();
        assertAll(
                () -> assertSame(existente, enviado),
                () -> assertEquals(ID, enviado.getIdProduct().id()),
                () -> assertEquals(dados.idBrand(), enviado.getIdBrand()),
                () -> assertEquals(dados.idDistributor(), enviado.getIdDistributor()),
                () -> assertEquals(dados.typeProduct(), enviado.getTypeProduct()),
                () -> assertEquals(dados.descriptionProduct(), enviado.getDescriptionProduct()),
                () -> assertEquals(dados.dataCreatedProduct(), enviado.getDataCreatedProduct()),
                () -> assertEquals(dados.dataDueProduct(), enviado.getDataDueProduct()),
                () -> assertEquals(dados.lotProduct(), enviado.getLotProduct()),
                () -> assertEquals(dados.priceBuy(), enviado.getPriceBuy()),
                () -> assertEquals(dados.priceSell(), enviado.getPriceSell()),
                () -> assertEquals(dados.stockProduct(), enviado.getStockProduct()),
                () -> assertEquals("Racao Premium", enviado.getNameProduct()),
                () -> assertEquals(TODAY, enviado.getDataCreated()),
                () -> assertTrue(enviado.isDataDueProductVerify()),
                () -> assertTrue(enviado.isProductEnableDue()),
                () -> assertEquals(2, enviado.getVersionProduct())
        );
    }

    @Test
    void naoDeveEditarProdutoInexistente() {
        when(repository.existsById(ID)).thenReturn(false);

        Exception falha = assertThrows(Exception.class, () -> edit.execute(editInput()));

        assertEquals("product is null", falha.getMessage());
        verify(repository).existsById(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void naoDeveEditarQuandoBuscaRetornaEntidadeNula() {
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.findById(ID)).thenReturn(null);

        Exception falha = assertThrows(Exception.class, () -> edit.execute(editInput()));

        assertEquals("entity is empty", falha.getMessage());
        verify(repository, never()).editProduct(any(Product.class));
    }

    @Test
    void devePropagarFalhaAoEditarProduto() {
        Product existente = produto(ID, false);
        RuntimeException falha = new RuntimeException("Falha ao editar");
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.findById(ID)).thenReturn(existente);
        when(repository.editProduct(existente)).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> edit.execute(editInput())));
    }

    @Test
    void deveExcluirProdutoExistente() throws Exception {
        when(repository.existsById(ID)).thenReturn(true);

        assertEquals("Product Deleted", delete.execute(ID));

        InOrder ordem = inOrder(repository);
        ordem.verify(repository).existsById(ID);
        ordem.verify(repository).delete(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void naoDeveExcluirProdutoInexistente() {
        when(repository.existsById(ID)).thenReturn(false);

        Exception falha = assertThrows(Exception.class, () -> delete.execute(ID));

        assertEquals("product is null", falha.getMessage());
        verify(repository).existsById(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoExcluirProduto() {
        RuntimeException falha = new RuntimeException("Falha ao excluir");
        when(repository.existsById(ID)).thenReturn(true);
        when(repository.delete(ID)).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> delete.execute(ID)));
    }

    @Test
    void deveListarProdutosDaMarcaConvertendoTodosOsDados() {
        UUID outroId = UUID.fromString("00000000-0000-0000-0000-000000000004");
        when(repository.findAllByProduct(BRAND_ID, PageRequest.of(1, 6)))
                .thenReturn(List.of(produto(ID, true), produto(outroId, false)));

        List<ProductOutput> resultado = findAll.execute(BRAND_ID, 1);

        verify(repository).findAllByProduct(BRAND_ID, PageRequest.of(1, 6));
        assertEquals(List.of(output(ID, true, 2), output(outroId, false, 2)), resultado);
    }

    @Test
    void deveRetornarListaVaziaQuandoMarcaNaoPossuiProdutos() {
        when(repository.findAllByProduct(BRAND_ID, PageRequest.of(0, 6))).thenReturn(List.of());

        assertEquals(List.of(), findAll.execute(BRAND_ID, 0));
        verify(repository).findAllByProduct(BRAND_ID, PageRequest.of(0, 6));
    }

    @Test
    void naoDeveListarProdutosComIdDeMarcaNulo() {
        RuntimeException falha = assertThrows(RuntimeException.class, () -> findAll.execute(null, 0));

        assertEquals("ID is null", falha.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoListarProdutos() {
        RuntimeException falha = new RuntimeException("Falha ao listar");
        when(repository.findAllByProduct(BRAND_ID, PageRequest.of(0, 6))).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> findAll.execute(BRAND_ID, 0)));
    }

    @Test
    void deveCriarNovaVersaoDepoisDeAlterarHabilitacaoDaAnterior() {
        Product persistido = produto(DISTRIBUTOR_ID, false);
        persistido.setVersionProduct(3);
        when(repository.save(any(Product.class))).thenReturn(persistido);

        ProductOutput resultado = newVersion.execute(versionInput());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        InOrder ordem = inOrder(repository);
        ordem.verify(repository).changeValueProductEnable(ID);
        ordem.verify(repository).save(captor.capture());
        ordem.verify(repository).existLot(persistido);
        Product enviado = captor.getValue();
        assertNotNull(enviado.getIdProduct().id());
        assertNotEquals(ID, enviado.getIdProduct().id());
        assertDadosProduto(enviado, false, 3);
        assertEquals(output(DISTRIBUTOR_ID, false, 3), resultado);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void naoDeveSalvarNovaVersaoQuandoFalhaAlteracaoDaAnterior() {
        RuntimeException falha = new RuntimeException("Falha na versao anterior");
        when(repository.changeValueProductEnable(ID)).thenThrow(falha);

        RuntimeException resultado = assertThrows(RuntimeException.class,
                () -> newVersion.execute(versionInput()));

        assertSame(falha, resultado.getCause());
        verify(repository).changeValueProductEnable(ID);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoSalvarNovaVersao() {
        RuntimeException falha = new RuntimeException("Falha ao salvar nova versao");
        when(repository.save(any(Product.class))).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> newVersion.execute(versionInput())));
        verify(repository).changeValueProductEnable(ID);
        verify(repository).save(any(Product.class));
    }

    @Test
    void deveConsultarMetricasDosProdutosConvertendoTodosOsDados() {
        UUID outroId = UUID.fromString("00000000-0000-0000-0000-000000000004");
        when(repository.metricsProduct(BRAND_ID, PageRequest.of(1, 12)))
                .thenReturn(List.of(produto(ID, true), produto(outroId, false)));

        List<ProductOutput> resultado = metrics.execute(BRAND_ID, 1);

        verify(repository).metricsProduct(BRAND_ID, PageRequest.of(1, 12));
        assertEquals(List.of(output(ID, true, 2), output(outroId, false, 2)), resultado);
    }

    @Test
    void deveRetornarMetricasVaziasQuandoNaoHaProdutos() {
        when(repository.metricsProduct(BRAND_ID, PageRequest.of(0, 12))).thenReturn(List.of());

        assertEquals(List.of(), metrics.execute(BRAND_ID, 0));
        verify(repository).metricsProduct(BRAND_ID, PageRequest.of(0, 12));
    }

    @Test
    void naoDeveConsultarMetricasDosProdutosComMarcaNula() {
        RuntimeException falha = assertThrows(RuntimeException.class, () -> metrics.execute(null, 0));

        assertEquals("idBrand is empty", falha.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void devePropagarFalhaAoConsultarMetricasDosProdutos() {
        RuntimeException falha = new RuntimeException("Falha nas metricas dos produtos");
        when(repository.metricsProduct(BRAND_ID, PageRequest.of(0, 12))).thenThrow(falha);

        assertSame(falha, assertThrows(RuntimeException.class, () -> metrics.execute(BRAND_ID, 0)));
    }

    @Test
    void deveExtrairInformacoesEnviandoTodasAsImagensParaIA() {
        ProductImageAIUseCase imageAI = imageUseCase();
        prepararRespostaIA();
        ProductImageAIOutput esperado = new ProductImageAIOutput("Racao Premium", "Alimento",
                "Racao para caes adultos", MANUFACTURE, DUE, "00123-A");
        when(response.entity(ProductImageAIOutput.class)).thenReturn(esperado);
        MultipartFile frente = new MockMultipartFile("frente", "frente.png", "image/png", new byte[]{1, 2});
        MultipartFile verso = new MockMultipartFile("verso", "verso.jpg", "image/jpeg", new byte[]{3, 4});

        assertEquals(esperado, imageAI.extractInfoFromImage(new MultipartFile[]{frente, verso}));

        ArgumentCaptor<UserMessage> captor = ArgumentCaptor.forClass(UserMessage.class);
        verify(request).messages(captor.capture());
        UserMessage mensagem = captor.getValue();
        assertEquals(2, mensagem.getMedia().size());
        assertEquals(MimeTypeUtils.IMAGE_PNG, mensagem.getMedia().get(0).getMimeType());
        assertEquals(MimeTypeUtils.IMAGE_JPEG, mensagem.getMedia().get(1).getMimeType());
        assertArrayEquals(new byte[]{1, 2}, (byte[]) mensagem.getMedia().get(0).getData());
        assertArrayEquals(new byte[]{3, 4}, (byte[]) mensagem.getMedia().get(1).getData());
        assertTrue(mensagem.getText().contains("nameProduct"));
        verify(response).entity(ProductImageAIOutput.class);
        verifyNoInteractions(chatModel, repository);
    }

    @Test
    void devePropagarFalhaAoExtrairInformacoesComIA() {
        ProductImageAIUseCase imageAI = imageUseCase();
        prepararRespostaIA();
        RuntimeException falha = new RuntimeException("IA indisponivel");
        when(response.entity(ProductImageAIOutput.class)).thenThrow(falha);
        MultipartFile imagem = new MockMultipartFile("imagem", "produto.png", "image/png", new byte[]{1});

        assertSame(falha, assertThrows(RuntimeException.class,
                () -> imageAI.extractInfoFromImage(new MultipartFile[]{imagem})));
    }

    @Test
    void naoDeveChamarIAQuandoImagemNaoPossuiTipoDeConteudo() {
        ProductImageAIUseCase imageAI = imageUseCase();
        MultipartFile imagem = new MockMultipartFile("imagem", "produto.png", null, new byte[]{1});

        assertThrows(NullPointerException.class,
                () -> imageAI.extractInfoFromImage(new MultipartFile[]{imagem}));
        verifyNoInteractions(chatClient, request, response, chatModel, repository);
    }

    @Test
    void naoDeveChamarIAQuandoTipoDeConteudoEInvalido() {
        ProductImageAIUseCase imageAI = imageUseCase();
        MultipartFile imagem = new MockMultipartFile("imagem", "produto.png", "invalido", new byte[]{1});

        assertThrows(IllegalArgumentException.class,
                () -> imageAI.extractInfoFromImage(new MultipartFile[]{imagem}));
        verifyNoInteractions(chatClient, request, response, chatModel, repository);
    }

    private ProductImageAIUseCase imageUseCase() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        return new ProductImageAIUseCase(chatModel, chatClientBuilder);
    }

    private void prepararRespostaIA() {
        when(chatClient.prompt()).thenReturn(request);
        when(request.messages(any(UserMessage.class))).thenReturn(request);
        when(request.call()).thenReturn(response);
    }

    private ProductInput input(boolean verificarValidade) {
        return new ProductInput(BRAND_ID, DISTRIBUTOR_ID, "Racao Premium", "Alimento",
                "Racao para caes adultos", MANUFACTURE, DUE, "00123-A", 40.5, 65.9, 20,
                verificarValidade, 2);
    }

    private ProductEditInput editInput() {
        return new ProductEditInput(ID,
                UUID.fromString("00000000-0000-0000-0000-000000000005"),
                UUID.fromString("00000000-0000-0000-0000-000000000006"),
                "Higiene", "Shampoo para caes", MANUFACTURE.plusDays(1), DUE.plusDays(1),
                "00456-B", 25.5, 45.9, 35);
    }

    private ProductNewVersionInput versionInput() {
        ProductInput dados = input(false);
        return new ProductNewVersionInput(ID, dados.idBrand(), dados.idDistributor(),
                dados.nameProduct(), dados.typeProduct(), dados.descriptionProduct(),
                dados.dataCreatedProduct(), dados.dataDueProduct(), dados.lotProduct(),
                dados.priceBuy(), dados.priceSell(), dados.stockProduct(),
                dados.dataDueProductVerify(), dados.versionProduct());
    }

    private Product produto(UUID id, boolean verificarValidade) {
        return new Product(IdProduct.toEdit(id), BRAND_ID, DISTRIBUTOR_ID, "Racao Premium", "Alimento",
                "Racao para caes adultos", TODAY, MANUFACTURE, DUE, "00123-A", 40.5, 65.9, 20,
                verificarValidade, 2, true);
    }

    private ProductOutput output(UUID id, boolean verificarValidade, long versao) {
        // Valores esperados independentes de ProductOutput.from para detectar erros no mapeamento.
        return new ProductOutput(id, BRAND_ID, DISTRIBUTOR_ID, "Racao Premium", "Alimento",
                "Racao para caes adultos", TODAY, MANUFACTURE, DUE, "00123-A", 40.5, 65.9, 20,
                verificarValidade, versao);
    }

    private void assertDadosProduto(Product produto, boolean verificarValidade, long versao) {
        assertAll(
                () -> assertEquals(BRAND_ID, produto.getIdBrand()),
                () -> assertEquals(DISTRIBUTOR_ID, produto.getIdDistributor()),
                () -> assertEquals("Racao Premium", produto.getNameProduct()),
                () -> assertEquals("Alimento", produto.getTypeProduct()),
                () -> assertEquals("Racao para caes adultos", produto.getDescriptionProduct()),
                () -> assertEquals(MANUFACTURE, produto.getDataCreatedProduct()),
                () -> assertEquals(DUE, produto.getDataDueProduct()),
                () -> assertEquals("00123-A", produto.getLotProduct()),
                () -> assertEquals(40.5, produto.getPriceBuy()),
                () -> assertEquals(65.9, produto.getPriceSell()),
                () -> assertEquals(20, produto.getStockProduct()),
                () -> assertEquals(verificarValidade, produto.isDataDueProductVerify()),
                () -> assertEquals(versao, produto.getVersionProduct()),
                () -> assertTrue(produto.isProductEnableDue())
        );
    }
}
