package com.usetech.dvente.entities.products;

import com.usetech.dvente.entities.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table()
@Getter
@Setter
public class Keyword extends BaseModel {

    @Column(nullable = false, unique = true)
    private String name;

    private String color;

    private boolean popular = false;

    @Override
    public String toString() {
        return name;
    }
}
