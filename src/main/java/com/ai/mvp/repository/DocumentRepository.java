package com.ai.mvp.repository;

import com.ai.mvp.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Document entity
 *
 * Phase 3: Vector Database & Embeddings
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

    /**
     * Find documents by category
     */
    List<Document> findByCategory(String category);

    /**
     * Find documents by title containing (case-insensitive)
     */
    @Query("SELECT d FROM Document d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Document> findByTitleContaining(String keyword);

    /**
     * Count documents by category
     */
    long countByCategory(String category);
}
