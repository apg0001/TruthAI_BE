package jpabasic.truthaiserver.service.prompt;

import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.domain.Prompt;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.PromptAnswerDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.AnswerRepository;
import jpabasic.truthaiserver.repository.PromptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jpabasic.truthaiserver.exception.ErrorMessages;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static jpabasic.truthaiserver.domain.LLMModel.*;
import static jpabasic.truthaiserver.exception.ErrorMessages.PROMPT_GENERATE_ERROR;
import static jpabasic.truthaiserver.exception.ErrorMessages.PROMPT_NOT_FOUND;


@Service
@Slf4j
public class PromptService {

    private final PromptRepository promptRepository;
    private final PromptEngine promptEngine;
    private final AnswerRepository answerRepository;


    public PromptService(PromptRepository promptRepository, PromptEngine promptEngine,AnswerRepository answerRepository) {
        this.promptRepository = promptRepository;
        this.promptEngine = promptEngine;
        this.answerRepository = answerRepository;
    }


    //summary 생성해야 (생성형 ai 이용)
    @Transactional
    public void summarize(String text){

    }



    //최적화 프롬프트 없이 질문했을 때
    public void savePromptAnswer(String question, List<LlmAnswerDto> results, User user) {

        if(results==null || results.isEmpty()){
            throw new BusinessException(ErrorMessages.MESSAGE_NULL_ERROR);
        }

        //LlmAnswerDto -> Answer
        List<Answer> answers = results.stream()
                .map(dto -> dto.toEntity())
                .toList();

        try {
            Prompt prompt = new Prompt(question, answers, user);
            promptRepository.save(prompt);
        } catch (BusinessException e) {
            log.error(e.getMessage());
            throw new BusinessException(PROMPT_GENERATE_ERROR);
        }

    }


    //최적화 전 프롬프트 저장
    public Long saveOriginalPrompt(LlmRequestDto request,User user) {
        String originalPrompt=request.getQuestion();

        Prompt prompt=new Prompt(originalPrompt,new ArrayList<>(),user); //answer는 저장 전
        Prompt saved=promptRepository.save(prompt);
        return saved.getId();
    }

    //최적화 프롬프트 저장
    public void saveOptimizedPrompt(String optimizedPrompt, Long promptId){
        Prompt prompt=promptRepository.findById(promptId)
                .orElseThrow(()->new BusinessException(PROMPT_NOT_FOUND));
        prompt.optimize(optimizedPrompt);
        System.out.println("✅promptId"+prompt.getId());
        promptRepository.save(prompt);
    }


    //프롬프트 내용 요약
    public String summarizePrompts(String prompt){
        return promptEngine.execute("summarize",prompt);
    }

    public List<Map<LLMModel,String>> runByModel(LlmRequestDto request){
        Map<LLMModel,String> answer=request.getModels().stream()
                .map(LLMModel::fromString)
                .collect(toMap(
                        Function.identity(),
                        (LLMModel model)->switch(model) {
                            case GPT ->
                                    promptEngine.getOptimizedAnswerByGpt("optimized", new Message(request.getQuestion()), request.getPersona(), request.getPromptDomain());
                            case CLAUDE ->
                                    promptEngine.getOptimizedAnswerByClaude("optimized", new Message(request.getQuestion()), request.getPersona(), request.getPromptDomain());
                            case GEMINI ->
                                    promptEngine.getOptimizedAnswerByGemini("optimized", new Message(request.getQuestion()), request.getPersona(), request.getPromptDomain());
                            case PERPLEXITY -> null;
                        },
                        (a,b)->a,
                        ()->new EnumMap<>(LLMModel.class)));


                return List.of(answer);
    }

    //최적화 프롬프트 실행(현재는 gpt로만) //⭐병렬 처리 위한 함수
    public void optimizingPrompt(LlmRequestDto dto,User user,Long promptId) {
        //최적화된 프롬프트 반환
        List<Message> answer= promptEngine.getOptimizedPrompt("optimized",dto);

        //모델 리스트 String -> ENUM
        List<String> models=dto.getModels();
        List<LLMModel> llmModels=models.stream()
                        .map(LLMModel::fromString)
                                .toList();




////        return answer;
    }


    //최적화된 프롬프트 반환
    public List<Message> getOptimizedPrompt(LlmRequestDto dto,Long promptId) {
//        Long promptId=saveOriginalPrompt(dto,user);
        String templateKey=dto.getTemplateKey();

        List<Message> optimizedPrompt=promptEngine.getOptimizedPrompt(templateKey,dto);

        //db에 저장은 String type으로 ? -> List<Message>로 수정할수도.
        String optimizedPromptSt=optimizedPrompt.toString();
        saveOptimizedPrompt(optimizedPromptSt,promptId);

        return optimizedPrompt;
    }

    //LLM 응답 저장
    @Transactional
    public void saveAnswers(List<Map<LLMModel, PromptAnswerDto>> raw,User user) {

        raw.forEach(map->mapping(map,user));

    }

    //여러 개의 모델-답변 리스트 분리
    public void mapping(Map<LLMModel,PromptAnswerDto> map,User user) {
       map.forEach((model,dto)->saveOne(model,dto,user));
    }

    //Answer Entity db에 저장
    public void saveOne(LLMModel model,PromptAnswerDto dto,User user) {

        Prompt prompt=promptRepository.findById(dto.promptId())
                .orElseThrow(()->new BusinessException(PROMPT_NOT_FOUND));

        String content=dto.answer();
        Answer entity=new Answer(content,model,prompt,user);
        answerRepository.save(entity);

    }




}
