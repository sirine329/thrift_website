package com.thriftby.repository;

import com.thriftby.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    List<Categorie> findByActifTrue();
    Optional<Categorie> findByNom(String nom);
    boolean existsByNom(String nom);
}
