package com.SystemPetShop.brand.infrastructure.http;

import com.SystemPetShop.brand.application.BrandDeleteUseCase;
import com.SystemPetShop.brand.application.BrandEditUseCase;
import com.SystemPetShop.brand.application.BrandFindAllUseCase;
import com.SystemPetShop.brand.application.BrandMetricsUseCase;
import com.SystemPetShop.brand.application.BrandMetricsSearchUseCase;
import com.SystemPetShop.brand.application.BrandSearchUseCase;
import com.SystemPetShop.brand.application.BrandSaveUseCase;
import com.SystemPetShop.brand.infrastructure.http.dto.BrandDto;
import com.SystemPetShop.brand.infrastructure.http.dto.BrandEditDto;
import com.SystemPetShop.brand.infrastructure.http.dto.BrandFindAllByDistributorDto;
import com.SystemPetShop.brand.infrastructure.http.response.BrandReponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RequestMapping("/brand")
@RestController
public class BrandController {
    private final BrandSaveUseCase brandSaveUseCase;
    private final BrandFindAllUseCase brandFindAllUseCase;
    private final BrandDeleteUseCase brandDeleteUseCase;
    private final BrandEditUseCase brandEditUseCase;
    private final BrandMetricsUseCase brandMetricsUseCase;
    private final BrandMetricsSearchUseCase brandMetricsSearchUseCase;
    private final BrandSearchUseCase brandSearchUseCase;

    public BrandController(BrandSaveUseCase brandSaveUseCase, BrandFindAllUseCase brandFindAllUseCase,
                           BrandDeleteUseCase brandDeleteUseCase, BrandEditUseCase brandEditUseCase,
                           BrandMetricsUseCase brandMetricsUseCase, BrandMetricsSearchUseCase brandMetricsSearchUseCase,
                           BrandSearchUseCase brandSearchUseCase) {
        this.brandSaveUseCase = brandSaveUseCase;
        this.brandFindAllUseCase = brandFindAllUseCase;
        this.brandDeleteUseCase = brandDeleteUseCase;
        this.brandEditUseCase = brandEditUseCase;
        this.brandMetricsUseCase = brandMetricsUseCase;
        this.brandMetricsSearchUseCase = brandMetricsSearchUseCase;
        this.brandSearchUseCase = brandSearchUseCase;
    }

    @GetMapping("/search")
    public List<BrandReponse> searchBrand(@RequestParam("name") String name,
                                             @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return brandSearchUseCase.execute(name, page).stream()
                    .map(BrandReponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/metrics/search")
    public List<BrandReponse> searchBrandMetrics(@RequestParam("name") String name,
                                                @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return brandMetricsSearchUseCase.execute(name, page).stream()
                    .map(BrandReponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BrandReponse> save(@RequestBody BrandDto brandDto){

        var result = brandSaveUseCase.execute(brandDto.toInput());

        return ResponseEntity.ok().body(BrandReponse.from(result));
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BrandReponse>> findAllByDistributor(@RequestParam UUID idDistributor,
                                                                  @RequestParam(defaultValue = "0") int page) throws Exception {
        var list = brandFindAllUseCase.execute(idDistributor, page).stream().map(BrandReponse::from)
                .toList();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/metrics")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BrandReponse>> findAllByDistributorMetrics(
            @RequestParam UUID idDistributor, @RequestParam(defaultValue = "0") int page) throws Exception {
        var list = brandFindAllUseCase.executeMetrics(idDistributor, page).stream().map(BrandReponse::from)
                .toList();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/metricsTotal")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<BrandReponse>> findAllByDistributorMetricsTotal(
            @RequestParam UUID idDistributor) throws Exception {
        var list = brandMetricsUseCase.findByMetricsTotal(idDistributor).stream().map(BrandReponse::from)
                .toList();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/metricBrand")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Double>> metricBrand(@RequestParam("idBrand") UUID idBrand) {
        return ResponseEntity.ok(brandMetricsUseCase.execute(idBrand));
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> deleteBrand(@RequestParam UUID id) throws Exception {
        return ResponseEntity.accepted().body(brandDeleteUseCase.execute(id));

    }

    @PutMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> editBrand(@RequestBody BrandEditDto brandEditDto) throws Exception {
        return ResponseEntity.ok().body(brandEditUseCase.execute(BrandEditDto.toInput(brandEditDto)));
    }
}
