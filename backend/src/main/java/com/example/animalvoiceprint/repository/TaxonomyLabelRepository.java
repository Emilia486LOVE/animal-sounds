package com.example.animalvoiceprint.repository;

import com.example.animalvoiceprint.entity.TaxonomyLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxonomyLabelRepository extends JpaRepository<TaxonomyLabel, Integer> {
    List<TaxonomyLabel> findByParentId(Integer parentId);
    List<TaxonomyLabel> findByTaxonRank(String taxonRank);
    List<TaxonomyLabel> findByLabelName(String labelName);
    List<TaxonomyLabel> findByLabelPathContaining(String path);
    List<TaxonomyLabel> findByParentIdOrderByLabelName(Integer parentId);
}