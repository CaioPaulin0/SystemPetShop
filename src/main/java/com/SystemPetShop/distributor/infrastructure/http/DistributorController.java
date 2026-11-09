package com.SystemPetShop.distributor.infrastructure.http;

import com.SystemPetShop.distributor.application.*;
import com.SystemPetShop.distributor.infrastructure.http.dto.DistributorDto;
import com.SystemPetShop.distributor.infrastructure.http.dto.DistributorEditDto;
import com.SystemPetShop.distributor.infrastructure.http.response.DistributorReponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/distributor")
public class DistributorController {

    private final DistributorSaveUseCase distributorSaveUseCase;
    private final DistributorFindAllUseCase distributorFindAllUseCase;
    private final DistributorDeleteUseCase distributorDeleteUseCase;
    private final DistributorEditUseCase distributorEditUseCase;
    private final DistributorMetricsUseCase distributorMetricsUseCase;
    private final DistributorMetricsSearchUseCase distributorMetricsSearchUseCase;
    private final DistributorSearchUseCase distributorSearchUseCase;

    public DistributorController(
            DistributorSaveUseCase distributorSaveUseCase,
            DistributorFindAllUseCase distributorFindAllUseCase,
            DistributorDeleteUseCase distributorDeleteUseCase,
            DistributorEditUseCase distributorEditUseCase,
            DistributorMetricsUseCase distributorMetricsUseCase,
            DistributorMetricsSearchUseCase distributorMetricsSearchUseCase,
            DistributorSearchUseCase distributorSearchUseCase) {
        this.distributorSaveUseCase = distributorSaveUseCase;
        this.distributorFindAllUseCase = distributorFindAllUseCase;
        this.distributorDeleteUseCase = distributorDeleteUseCase;
        this.distributorEditUseCase = distributorEditUseCase;
        this.distributorMetricsUseCase = distributorMetricsUseCase;
        this.distributorMetricsSearchUseCase = distributorMetricsSearchUseCase;
        this.distributorSearchUseCase = distributorSearchUseCase;
    }

    @GetMapping("/search")
    public List<DistributorReponse> searchDistributor(@RequestParam("name") String name,
                                             @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return distributorSearchUseCase.execute(name, page).stream()
                    .map(DistributorReponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/metrics/search")
    public List<DistributorReponse> searchDistributorMetrics(@RequestParam("name") String name,
                                                            @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return distributorMetricsSearchUseCase.execute(name, page).stream()
                    .map(DistributorReponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/teste")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> teste(){
        return ResponseEntity.ok().body("alo");
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<DistributorReponse> allDistributor(@RequestParam int page) throws Exception {
        return distributorFindAllUseCase.execute(page).stream()
                .map(DistributorReponse::from)
                .toList();
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public DistributorReponse save(@RequestBody DistributorDto distributorDto){
        var result = distributorSaveUseCase.execute(distributorDto.toInput());
        return DistributorReponse.from(result);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deletedDistributor(@RequestParam UUID id) throws Exception {

        return ResponseEntity.ok().body(distributorDeleteUseCase.execute(id));
    }

    @PutMapping("/edit")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> editDistributor(@RequestBody DistributorEditDto distributorEditDto) throws Exception {
        return ResponseEntity.ok().body(distributorEditUseCase.execute(distributorEditDto.toInputEdit()));
    }

    @GetMapping("/metricDistributor")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<Double>> metricDistributor(@RequestParam("idDistributor") UUID idDistributor) {
        return ResponseEntity.ok(distributorMetricsUseCase.execute(idDistributor));
    }

    @GetMapping("/metricDistributorTotal")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<DistributorReponse>> metricDistributorTotal() throws Exception {
        return ResponseEntity.ok(distributorFindAllUseCase.executeMetricsTotal().stream()
                .map(DistributorReponse::from)
                .toList());
    }

    @GetMapping("/metrics")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<DistributorReponse>> findAllDistributorMetrics(
            @RequestParam(defaultValue = "0") int page) throws Exception {
        return ResponseEntity.ok(distributorFindAllUseCase.executeMetrics(page).stream()
                .map(DistributorReponse::from)
                .toList());
    }
}
