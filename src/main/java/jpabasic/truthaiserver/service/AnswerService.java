//package jpabasic.truthaiserver.service;
//
//import jpabasic.truthaiserver.domain.LLMModel;
//import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
//import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
//import jpabasic.truthaiserver.exception.BusinessException;
//import jpabasic.truthaiserver.exception.ErrorMessages;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static jpabasic.truthaiserver.domain.LLMModel.CLAUDE;
//import static jpabasic.truthaiserver.domain.LLMModel.GPT;
//import static jpabasic.truthaiserver.domain.LLMModel.GEMINI;
//import static jpabasic.truthaiserver.domain.LLMModel.PERPLEXITY;
//
//@Service
//@Slf4j
//public class AnswerService {
//
//    public List<LlmAnswerDto> getLlmAnswer(LlmRequestDto requestDto) {
//        List<LLMModel> models=requestDto.getModels();
//        String question=requestDto.getQuestion();
//
//        return models.stream()
//                //선택한 모델들에 대한 답변 수집
//                .map(model->selectAnswer(model,question))
//                .collect(Collectors.toList());
//    }
//
//
//    public LlmAnswerDto selectAnswer(LLMModel model,String question){
//        return switch (model) {
//            case GPT -> new LlmAnswerDto(GPT, createGptAnswer(question));
//            case CLAUDE -> new LlmAnswerDto(CLAUDE, createAnswer(CLAUDE));
//            case PERPLEXITY -> new LlmAnswerDto(PERPLEXITY, createAnswer(PERPLEXITY));
//            case GEMINI -> new LlmAnswerDto(GEMINI, createAnswer(GEMINI));
//            default -> throw new BusinessException(ErrorMessages.LLM_MODEL_ERROR);
//        };
//    }
//
//    public String createAnswer(LLMModel model){
//
//    }
//
//    public String createGptAnswer(String question){
//
//    }
//}
