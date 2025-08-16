package jpabasic.truthaiserver.repository;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Answer findByPromptIdAndModel(Long promptId, LLMModel model);
    List<Answer> findByPromptId(Long promptId);
}
