package com.SystemPetShop.product.application;

import com.SystemPetShop.product.application.output.ProductImageAIOutput;
import com.SystemPetShop.product.application.output.ProductOutput;
import com.SystemPetShop.product.domain.Product;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.core.io.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ProductImageAIUseCase {

    private final ChatModel chatModel;
    private final ChatClient chatClient;

    public ProductImageAIUseCase(ChatModel chatModel, ChatClient.Builder chatClientBuilder) {
        this.chatModel = chatModel;
        this.chatClient = chatClientBuilder.build();
    }

    public ProductImageAIOutput extractInfoFromImage(MultipartFile[] imgs){

        List<Media> medias = Arrays.stream(imgs)
                .map(img -> new Media(
                        MimeTypeUtils.parseMimeType(
                                Objects.requireNonNull(img.getContentType())
                        ),
                        img.getResource()
                ))
                .toList();


        UserMessage userMessage = UserMessage.builder()
                .text("\"\"\"\n" +
                        "                    Você é um sistema especializado em análise visual\n" +
                        "                    de embalagens e rótulos de produtos.\n" +
                        "\n" +
                        "                    Todas as imagens recebidas pertencem ao MESMO produto.\n" +
                        "\n" +
                        "                    Analise todas as imagens em conjunto.\n" +
                        "\n" +
                        "                    Extraia:\n" +
                        "\n" +
                        "                    nameProduct:\n" +
                        "                    Nome comercial do produto.\n" +
                        "\n" +
                        "                    typeProduct:\n" +
                        "                    Tipo ou categoria do produto.\n" +
                        "\n" +
                        "                    descriptionProduct:\n" +
                        "                    Descrição curta do produto baseada somente\n" +
                        "                    nas informações visíveis nas imagens.\n" +
                        "\n" +
                        "                    dataManufactureProduct:\n" +
                        "                    Data de fabricação.\n" +
                        "\n" +
                        "                    dataDueProduct:\n" +
                        "                    Data de validade.\n" +
                        "\n" +
                        "                    lotProduct:\n" +
                        "                    Número ou código do lote como texto, preservando\n" +
                        "                    letras, separadores e zeros à esquerda.\n" +
                        "\n" +
                        "                    REGRAS:\n" +
                        "\n" +
                        "                    - Analise todas as imagens antes de responder.\n" +
                        "                    - Não invente informações.\n" +
                        "                    - Use informações de diferentes imagens para\n" +
                        "                      complementar os dados.\n" +
                        "                    - Não confunda fabricação com validade.\n" +
                        "                    - Não confunda código de barras com lote.\n" +
                        "                    - FAB, FABRICAÇÃO ou MFG indicam fabricação.\n" +
                        "                    - VAL, VALIDADE ou EXP indicam validade.\n" +
                        "                    - LOT ou LOTE indicam lote.\n" +
                        "                    - Caso o lote não seja encontrado ou esteja\n" +
                        "                      ilegível, retorne \"000\".\n" +
                        "                    - Caso outro campo não possa ser identificado\n" +
                        "                      com segurança, retorne null.\n" +
                        "                    - Quando houver uma data completa, normalize para\n" +
                        "                      YYYY-MM-DD.\n" +
                        "                    - Se houver apenas mês e ano, preserve a informação\n" +
                        "                      encontrada.\n" +
                        "                    \"\"\"")
                .media(medias)
                .build();

        ProductImageAIOutput response = chatClient
                .prompt()
                .messages(userMessage)
                .call()
                .entity(ProductImageAIOutput.class);


       return response;
    }
}
