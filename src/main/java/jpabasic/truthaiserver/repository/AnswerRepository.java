package jpabasic.truthaiserver.repository;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Answer findByPromptIdAndModel(Long promptId, LLMModel model);
    List<Answer> findByPromptId(Long promptId);

    @Query("""

            SELECT DISTINCT a.prompt.id FROM Answer a
            WHERE a.user.id=:userId
            AND a.score IS NOT NULL
            ORDER BY a.prompt.createdAt DESC
            """)
    List<Long> findAllPromptIdsByUserId(@Param("userId") Long userId, Pageable pageable);

}
