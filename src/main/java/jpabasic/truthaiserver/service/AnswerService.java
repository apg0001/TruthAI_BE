package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.repository.AnswerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static jpabasic.truthaiserver.domain.LLMModel.*;

@Service
@Slf4j
public class AnswerService {


    private final LlmService llmService;
    private final AnswerRepository answerRepository;

    public AnswerService(LlmService llmService,AnswerRepository answerRepository) {
        this.llmService=llmService;
        this.answerRepository=answerRepository;
    }




    public List<LlmAnswerDto> selectAnswer(List<LLMModel> models, String question) {

        if (models == null || models.isEmpty()) {
            throw new BusinessException(ErrorMessages.LLM_NULL_ERROR);
        }

        return models.stream()
                .map(model -> switch (model) {
                    case GPT -> toDto(GPT, llmService.createGptAnswer(question));
                    case CLAUDE -> toDto(CLAUDE, llmService.createClaudeAnswer(question));
                    case PERPLEXITY -> toDto(PERPLEXITY, llmService.createPerplexityAnswer(question));
                    case GEMINI -> toDto(GEMINI, llmService.createGeminiAnswer(question));
                    default -> throw new BusinessException(ErrorMessages.LLM_MODEL_ERROR);
                })
                .toList();
    }


    public LlmAnswerDto toDto(LLMModel model,String answer){

        LlmAnswerDto answerDto = new LlmAnswerDto(model,answer);
//        Answer answerEntity=answerDto.toEntity();
//        answerRepository.save(answerEntity);
        return answerDto;

    }



}
