package com.SystemPetShop.expired.infrastructure.repository;

import com.SystemPetShop.product.infrastructure.entity.ProductEntity;

public interface ExpiredProductProjection {
    ProductEntity getProduct();

    String getNameBrand();

    String getNameDistributor();
}
