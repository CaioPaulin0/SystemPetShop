package com.SystemPetShop.shared.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class NameSearch {
    private NameSearch() {
    }

    public static <T, R> List<R> execute(String name, int page, Sort sort,
                                        BiFunction<String, Pageable, List<T>> search,
                                        Function<T, R> mapper) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        return search.apply(name.strip(), PageRequest.of(page, 6, sort)).stream()
                .map(mapper)
                .toList();
    }
}
