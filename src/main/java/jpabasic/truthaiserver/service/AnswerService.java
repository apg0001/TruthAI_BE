package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.repository.AnswerRepository;
import jpabasic.truthaiserver.service.claude.ClaudeService;
import jpabasic.truthaiserver.service.gpt.GptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static jpabasic.truthaiserver.domain.LLMModel.*;

@Service
@Slf4j
public class AnswerService {


    private final LlmService llmService;
    private final ClaudeService claudeService;
    private final GptService gptService;

    public AnswerService(LlmService llmService, ClaudeService claudeService, GptService gptService) {
        this.llmService=llmService;
        this.claudeService=claudeService;
        this.gptService = gptService;
    }




    public List<LlmAnswerDto> selectAnswer(List<LLMModel> models, String question) {

        if (models == null || models.isEmpty()) {
            throw new BusinessException(ErrorMessages.LLM_NULL_ERROR);
        }

        return models.stream()
                .map(model -> switch (model) {
                    case GPT -> toDto(GPT, gptService.createGptAnswer(question));
                    case CLAUDE -> toDto(CLAUDE, claudeService.createClaudeAnswer(question));
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
