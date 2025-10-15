package com.usetech.dvente.entities.products;

import com.usetech.dvente.entities.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_images")
@Getter
@Setter
public class ProductImage extends BaseModel {

    private String image;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}