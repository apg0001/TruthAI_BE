package jpabasic.truthaiserver.service.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.*;
import jpabasic.truthaiserver.dto.answer.AnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.LLMResponseDto;
import jpabasic.truthaiserver.dto.prompt.PromptAnswerDto;
import jpabasic.truthaiserver.dto.prompt.PromptResultDto;
import jpabasic.truthaiserver.dto.sources.SourcesDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.AnswerRepository;
import jpabasic.truthaiserver.repository.PromptRepository;
import jpabasic.truthaiserver.service.sources.SourcesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jpabasic.truthaiserver.exception.ErrorMessages;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static jpabasic.truthaiserver.exception.ErrorMessages.*;


@Service
@Slf4j
public class PromptService {

    private final PromptRepository promptRepository;
    private final PromptEngine promptEngine;
    private final AnswerRepository answerRepository;
    private final SourcesService sourcesService;


    public PromptService(
            PromptRepository promptRepository,
            PromptEngine promptEngine,AnswerRepository answerRepository,SourcesService sourcesService) {
        this.promptRepository = promptRepository;
        this.promptEngine = promptEngine;
        this.answerRepository = answerRepository;
        this.sourcesService = sourcesService;
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

    public List<Map<LLMModel,LLMResponseDto>> runByModel(LlmRequestDto request){
        Map<LLMModel,?> answer=request.getModels().stream()
                .map(LLMModel::fromString)
                .collect(toMap(
                        Function.identity(),
                        (LLMModel model)->switch(model) {
                            case GPT -> {
                                try {
                                    yield promptEngine.getStructuredAnswerByGpt("optimized", new Message(request.getQuestion()), request.getPersona(), request.getPromptDomain());
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case CLAUDE -> {
                                try {
                                    yield promptEngine.getStructuredAnswerByClaude("optimized", new Message(request.getQuestion()), request.getPersona(), request.getPromptDomain());
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case GEMINI -> {
                                try {
                                    yield promptEngine.getStructuredAnswerByClaude("optimized", new Message(request.getQuestion()), request.getPersona(), request.getPromptDomain());
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case PERPLEXITY -> null;
                        },
                        (a,b)->a,
                        ()->new EnumMap<>(LLMModel.class)));


                return List.of((Map<LLMModel, LLMResponseDto>) answer);
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
    public List<Map<LLMModel,PromptResultDto>> saveAnswers(List<Map<LLMModel, LLMResponseDto>> raw, User user,Long promptId) {

//        raw.forEach(map->mapping(map,user));
        return raw.stream()
                .map(map->mapping(map,user,promptId))
                .toList();
    }

    public Map<LLMModel,PromptResultDto> mapping(Map<LLMModel,LLMResponseDto> map,User user,Long promptId) {
       return map.entrySet().stream()
               .collect(toMap(
                       Map.Entry::getKey,
                       e->saveOne(e.getKey(),e.getValue(),user,promptId),
                       (a,b)->a,
                       ()->new EnumMap<>(LLMModel.class)
               ));
    }

    public PromptResultDto saveOne(LLMModel model,LLMResponseDto dto,User user,Long promptId) {

        Prompt prompt=promptRepository.findById(promptId)
                .orElseThrow(()->new BusinessException(PROMPT_NOT_FOUND));

        //answer 저장
        String content=dto.answer();
        Answer entity=new Answer(content,model,prompt,user);
        AnswerDto answerDto;

        try {
            answerRepository.save(entity);
            answerDto=AnswerDto.from(entity);
        }catch(BusinessException e) {
            throw new BusinessException(ANSWER_SAVE_ERROR);
        }

        Long answerId=entity.getId();

        //sources 저장
        List<SourcesDto> sources=sourcesService.saveSources(dto,answerId);

        PromptResultDto result=new PromptResultDto(answerDto,sources);
        return result;

    }




}
