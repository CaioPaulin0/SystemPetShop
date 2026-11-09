package com.SystemPetShop.expired.infrastructure.http;

import com.SystemPetShop.expired.application.ExpiredFindAllUseCase;
import com.SystemPetShop.expired.application.ExpiredFilterUseCase;
import com.SystemPetShop.expired.application.ExpiredSearchUseCase;
import com.SystemPetShop.expired.application.ExpiredVerifyUseCase;
import com.SystemPetShop.expired.infrastructure.http.response.ExpiredResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RequestMapping("/expired")
@RestController
public class ExpiredController {
    private final ExpiredFindAllUseCase expiredFindAllUseCase;
    private final ExpiredVerifyUseCase expiredVerifyUseCase;
    private final ExpiredSearchUseCase expiredSearchUseCase;
    private final ExpiredFilterUseCase expiredFilterUseCase;

    public ExpiredController(ExpiredFindAllUseCase expiredFindAllUseCase, ExpiredVerifyUseCase expiredVerifyUseCase,
                             ExpiredSearchUseCase expiredSearchUseCase, ExpiredFilterUseCase expiredFilterUseCase) {
        this.expiredFindAllUseCase = expiredFindAllUseCase;
        this.expiredVerifyUseCase = expiredVerifyUseCase;
        this.expiredSearchUseCase = expiredSearchUseCase;
        this.expiredFilterUseCase = expiredFilterUseCase;
    }

    @GetMapping("/filter")
    public List<ExpiredResponse> filterExpired(
            @RequestParam("dataDueProductVerify") boolean dataDueProductVerify,
            @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return expiredFilterUseCase.execute(dataDueProductVerify, page).stream()
                    .map(ExpiredResponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/search")
    public List<ExpiredResponse> searchExpired(@RequestParam("name") String name,
                                              @RequestParam(name = "page", defaultValue = "0") int page) {
        try {
            return expiredSearchUseCase.execute(name, page).stream()
                    .map(ExpiredResponse::from)
                    .toList();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/findAll")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ExpiredResponse>> findAllExpiredProduct(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(expiredFindAllUseCase.execute(page).stream()
                .map(ExpiredResponse::from)
                .toList());
    }

    @PutMapping("/productExpiredVerify")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> productDueVerify(@RequestParam UUID idProduct) throws Exception{
        return ResponseEntity.accepted().body(expiredVerifyUseCase.execute(idProduct));
    }


}
