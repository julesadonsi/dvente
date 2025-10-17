package com.usetech.dvente.repositories.products;

import com.usetech.dvente.entities.products.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByName(String name);
    List<Category> findByMain(Boolean main);
}
