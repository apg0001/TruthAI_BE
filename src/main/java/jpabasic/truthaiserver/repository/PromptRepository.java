package jpabasic.truthaiserver.repository;

import jpabasic.truthaiserver.domain.Prompt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    Optional<Prompt> findByIdAndUserId(Long id, Long userId);
    List<Prompt> findByFolderIdOrderByCreatedAtDesc(Long folderId);

    @Query("SELECT p FROM Prompt p WHERE p.user.id=:userId AND p.optimizedPrompt IS NOT NULL ORDER BY p.createdAt DESC")
    List<Prompt> findTop5ByUser_IdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT p FROM Prompt p WHERE p.user.id = :userId AND p.optimizedPrompt IS NOT NULL")
    List<Prompt> findPromptWithOptimizedPrompt(@Param("userId") Long userId);

    @Query("SELECT p FROM Prompt p " +
            "JOIN p.answers a " +
            "WHERE p.user.id = :userId " +
            "AND p.optimizedPrompt IS NOT NULL " +
            "AND a.score IS NOT NULL")
    List<Prompt> findPromptsWithAnswersAndScoreNotNull(@Param("userId") Long userId);

    @Query("SELECT p FROM Prompt p JOIN p.answers a WHERE p.user.id=:userId AND a.score IS NOT NULL ORDER BY p.createdAt DESC")
    List<Prompt> findPromptsWithAnswersAndScoreNull(@Param("userId") Long userId, Pageable pageable);
}
