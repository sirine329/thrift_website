package com.thriftby.service;

import com.thriftby.entity.Categorie;
import com.thriftby.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieService {

    private final CategorieRepository categorieRepository;

    @Transactional(readOnly = true)
    public List<Categorie> findAll()     { return categorieRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Categorie> findActives() { return categorieRepository.findByActifTrue(); }

    @Transactional(readOnly = true)
    public Categorie findById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Catégorie introuvable: " + id));
    }

    public Categorie save(Categorie categorie) {
        if (categorieRepository.existsByNom(categorie.getNom())) {
            throw new RuntimeException("Catégorie déjà existante: " + categorie.getNom());
        }
        return categorieRepository.save(categorie);
    }

    public Categorie update(Categorie categorie) {
        Categorie existing = findById(categorie.getId());
        existing.setNom(categorie.getNom());
        existing.setDescription(categorie.getDescription());
        existing.setIcone(categorie.getIcone());
        existing.setCouleurHex(categorie.getCouleurHex());
        existing.setActif(categorie.isActif());
        return categorieRepository.save(existing);
    }

    public void delete(Long id)        { categorieRepository.deleteById(id); }

    public void toggleActif(Long id) {
        Categorie c = findById(id);
        c.setActif(!c.isActif());
        categorieRepository.save(c);
    }

    public long countTotal() { return categorieRepository.count(); }
}
