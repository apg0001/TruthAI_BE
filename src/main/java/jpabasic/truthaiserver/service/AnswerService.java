package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequest;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeResponse;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiResponseDto;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptResponse;
import jpabasic.truthaiserver.dto.prompt.PromptAnswerDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.repository.AnswerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static jpabasic.truthaiserver.domain.LLMModel.CLAUDE;
import static jpabasic.truthaiserver.domain.LLMModel.GPT;
import static jpabasic.truthaiserver.domain.LLMModel.GEMINI;
import static jpabasic.truthaiserver.domain.LLMModel.PERPLEXITY;
import static jpabasic.truthaiserver.exception.ErrorMessages.PROMPT_NOT_FOUND;

@Service
@Slf4j
public class AnswerService {


    private final LlmService llmService;
    private final AnswerRepository answerRepository;

    public AnswerService(LlmService llmService,AnswerRepository answerRepository) {
        this.llmService=llmService;
        this.answerRepository=answerRepository;
    }


    public List<LlmAnswerDto> getLlmAnswers(List<LLMModel> models,String question) {

        return selectAnswer(models, question);
    }


    public List<LlmAnswerDto> selectAnswer(List<LLMModel> models, String question) {

        if (models == null || models.isEmpty()) {
            throw new BusinessException(ErrorMessages.LLM_NULL_ERROR);
        }

        return models.stream()
                .map(model -> switch (model) {
                    case GPT -> saveLlmAnswer(GPT, llmService.createGptAnswer(question));
                    case CLAUDE -> saveLlmAnswer(CLAUDE, llmService.createClaudeAnswer(question));
                    //            case PERPLEXITY -> new LlmAnswerDto(PERPLEXITY, createAnswer(PERPLEXITY));
                    case GEMINI -> saveLlmAnswer(GEMINI, llmService.createGeminiAnswer(question));
                    default -> throw new BusinessException(ErrorMessages.LLM_MODEL_ERROR);
                })
                .toList();
    }


    public LlmAnswerDto saveLlmAnswer(LLMModel model,String answer){

        LlmAnswerDto answerDto = new LlmAnswerDto(model,answer);
        Answer answerEntity=answerDto.toEntity();
        answerRepository.save(answerEntity);
        return answerDto;

    }


    public Answer getAnswer(PromptAnswerDto dto) {
        Long promptId = dto.promptId();
        Answer answer = null;
        try {
            answer = answerRepository.findByPromptIdAndModel(promptId, GPT);
        } catch (Exception e) {
            new BusinessException(ErrorMessages.MESSAGE_NULL_ERROR);
        }
        return answer;
    }



}
