package com.SystemPetShop.product.infrastructure.http;

import com.SystemPetShop.product.application.*;
import com.SystemPetShop.product.infrastructure.http.dto.ProductNewVersionDto;
import com.SystemPetShop.product.infrastructure.http.dto.ProductoDto;
import com.SystemPetShop.product.infrastructure.http.dto.ProductEditDto;
import com.SystemPetShop.product.infrastructure.http.response.ProductImageAIResponse;
import com.SystemPetShop.product.infrastructure.http.response.ProductResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequestMapping("/product")
@RestController
public class ProductController {
    private final ProductSaveUseCase productSaveUseCase;
    private final ProductFindAllUseCase productFindAllUseCase;
    private final ProductDeleteUseCase productDeleteUseCase;
    private final ProductEditUseCase productEditUseCase;
    private final ProductImageAIUseCase productImageAIUseCase;
    private final ProductNewVersionUseCase productNewVersionUseCase;
    private final ProductMetricUseCase productMetricUseCase;
    private final ProductMetricsSearchUseCase productMetricsSearchUseCase;
    private final ProductSearchUseCase productSearchUseCase;

    public ProductController(ProductSaveUseCase productSaveUseCase, ProductFindAllUseCase productFindAllUseCase, ProductDeleteUseCase productDeleteUseCase, ProductEditUseCase productEditUseCase, ProductImageAIUseCase productImageAIUseCase, ProductNewVersionUseCase productNewVersionUseCase, ProductMetricUseCase productMetricUseCase, ProductMetricsSearchUseCase productMetricsSearchUseCase, ProductSearchUseCase productSearchUseCase) {
        this.productSaveUseCase = productSaveUseCase;
        this.productFindAllUseCase = productFindAllUseCase;
        this.productDeleteUseCase = productDeleteUseCase;
        this.productEditUseCase = productEditUseCase;
        this.productImageAIUseCase = productImageAIUseCase;
        this.productNewVersionUseCase = productNewVersionUseCase;
        this.productMetricUseCase = productMetricUseCase;
        this.productMetricsSearchUseCase = productMetricsSearchUseCase;
        this.productSearchUseCase = productSearchUseCase;
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductoDto productoDto){
        return ResponseEntity.ok().body(ProductResponse.from(productSaveUseCase.execute(productoDto.toInput())));
    }

    @PostMapping("/newVersion")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProductResponse> newVersion(@RequestBody ProductNewVersionDto productoDto){
        return ResponseEntity.ok().body(ProductResponse.from(productNewVersionUseCase.execute(productoDto.toInput())));
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> findAllProduct(@RequestParam UUID idBrand,
                                                @RequestParam(defaultValue = "0") int page) {
        var result = productFindAllUseCase.execute(idBrand, page).stream()
                .map(ProductResponse::from)
                .toList();

        return result;
    }

    @GetMapping("/search")
    public List<ProductResponse> searchProduct(@RequestParam("name") String name,
                                             @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return productSearchUseCase.execute(name, page).stream()
                    .map(ProductResponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/metrics/search")
    public List<ProductResponse> searchProductMetrics(@RequestParam("name") String name,
                                               @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return productMetricsSearchUseCase.execute(name, page).stream()
                    .map(ProductResponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PutMapping("/edit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> editProduct(@RequestBody ProductEditDto productEditDto) throws Exception {
        var result = productEditUseCase.execute(productEditDto.toInput(productEditDto));

        return ResponseEntity.accepted().body(result);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deleteProduct(@RequestParam UUID idProduct) throws Exception {
        return ResponseEntity.ok().body(productDeleteUseCase.execute(idProduct));
    }

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<ProductImageAIResponse> extractData(@RequestParam("imgs") MultipartFile[] imgs) throws IOException {

        return ResponseEntity.accepted().body(ProductImageAIResponse.from(productImageAIUseCase.extractInfoFromImage(imgs)));
    }

    @GetMapping("/metricProduct")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> metricProduct(@RequestParam UUID idBrand,
                                               @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok().body(productMetricUseCase.execute(idBrand, page).stream().map(ProductResponse::from).toList()).getBody();
    }
}
