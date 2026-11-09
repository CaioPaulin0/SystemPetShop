package com.SystemPetShop.brand.infrastructure.entity;

import com.SystemPetShop.brand.domain.Brand;
import com.SystemPetShop.brand.domain.IdBrand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name ="brand_entity")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BrandEntity {
    @Id
    UUID idBrand;

    @Column(nullable = false, name = "id_distributor")
    UUID idDistributor;

    @Column(nullable = false, name = "name_brand", unique = true)
    String nameBrand;

    @Column(nullable = false, name = "type_brand")
    String typeBrand;

    public static BrandEntity from(Brand brand){
        return new BrandEntity(
                brand.idBrand.id(),
                brand.getIdDistributor(),
                brand.getNameBrand(),
                brand.getTypeBrand()
        );
    }

    public Brand toBrand(){
        return new Brand(
                new IdBrand(this.idBrand),
                this.idDistributor,
                this.nameBrand,
                this.typeBrand
        );
    }
}
