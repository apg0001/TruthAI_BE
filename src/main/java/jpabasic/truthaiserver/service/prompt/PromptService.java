package jpabasic.truthaiserver.service.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.*;
import jpabasic.truthaiserver.dto.answer.AnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.*;
import jpabasic.truthaiserver.dto.prompt.sidebar.SideBarPromptDto;
import jpabasic.truthaiserver.dto.prompt.sidebar.SideBarPromptListDto;
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
import java.util.stream.Collectors;

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




    //최적화 프롬프트 없이 질문했을 때
    @Transactional
    public Long savePromptAnswer(String question, List<LlmAnswerDto> results, User user,String summary) {

        if (results == null || results.isEmpty()) {
            throw new BusinessException(ErrorMessages.MESSAGE_NULL_ERROR);
        }

        //LlmAnswerDto -> Answer (prompt와 user는 Prompt 생성자에서 설정됨)
        List<Answer> answers = results.stream()
                .map(dto -> dto.toEntity())
                .toList();

        Long promptId;
        try {
            // Prompt 생성 시 생성자에서 각 Answer의 prompt와 user 관계를 자동 설정
            Prompt prompt = new Prompt(question, answers, user, summary);
            promptId = promptRepository.save(prompt).getId();
        } catch (BusinessException e) {
            log.error(e.getMessage());
            throw new BusinessException(PROMPT_SAVE_ERROR);
        }

        return promptId;
    }


    //최적화 전 프롬프트 저장
    @Transactional
    public Long saveOriginalPrompt(OptPromptRequestDto request, User user) {
        String originalPrompt=request.getQuestion();

        Prompt prompt=new Prompt(originalPrompt,new ArrayList<>(),user); //answer는 저장 전
        Prompt saved=promptRepository.save(prompt);
        return saved.getId();
    }

    //최적화 프롬프트 저장
    @Transactional
    public Long saveOptimizedPrompt(String optimizedPrompt, OptPromptRequestDto dto,User user,String summary){
        System.out.println(user.getUserBaseInfo());

        String originalPrompt=dto.getQuestion();
        Prompt prompt=new Prompt();
        prompt.assignUser(user);
        prompt.savePrompt(originalPrompt,optimizedPrompt,summary);
        Prompt saved=promptRepository.save(prompt);
        Long promptId=saved.getId();
        Long userId=saved.getUser().getId();

        System.out.println("⭐userId"+userId);
        return promptId;
    }


    //프롬프트 내용 요약
    public String summarizePrompts(String prompt){
        return promptEngine.execute("summarize",prompt);
    }


    @Transactional
    public List<Map<LLMModel, LLMResponseDto>> runByModel(LlmRequestDto request) {
        Message msg = new Message(request.getQuestion());
        String persona = request.getPersona();
        PromptDomain domain = request.getPromptDomain();

        Map<LLMModel, LLMResponseDto> answer = request.getModels().stream()
                .map(LLMModel::fromString)
                .collect(Collectors.toMap(
                        Function.identity(),
                        model -> invokeEngine(model, request.getTemplateKey(), msg, persona, domain),
                        (a, b) -> a,
                        () -> new EnumMap<>(LLMModel.class)
                ));

        return List.of(answer);
    }


    private LLMResponseDto invokeEngine(
            LLMModel model, String templateKey, Message msg, String persona, PromptDomain domain) {

        try {
            return switch (model) {
                case GPT        -> promptEngine.getStructuredAnswerByGpt(templateKey, msg, persona, domain);
                case CLAUDE     -> promptEngine.getStructuredAnswerByClaude(templateKey, msg, persona, domain);
                case GEMINI     -> null;
                case PERPLEXITY -> promptEngine.getStructuredAnswerByPerplexity(templateKey, msg, persona, domain);
            };
        } catch (JsonProcessingException e) {
            // 필요에 따라 로깅 추가
            throw new RuntimeException("Failed to parse JSON for model: " + model, e);
        }
    }




    //최적화된 프롬프트 반환
    @Transactional
    public List<Message> getOptimizedPrompt(OptPromptRequestDto dto) {
        String templateKey=dto.getTemplateKey();

        List<Message> optimizedPrompt=promptEngine.getOptimizedPrompt(templateKey,dto);

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

    @Transactional
    public Map<LLMModel,PromptResultDto> mapping(Map<LLMModel,LLMResponseDto> map,User user,Long promptId) {
       return map.entrySet().stream()
               .collect(toMap(
                       Map.Entry::getKey,
                       e->saveOne(e.getKey(),e.getValue(),user,promptId),
                       (a,b)->a,
                       ()->new EnumMap<>(LLMModel.class)
               ));
    }


    @Transactional
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

    //사이드바 리스트 조회
    public List<SideBarPromptListDto> checkSideBar(Long userId){

        List<Prompt> list=promptRepository.findTop5ByUser_IdOrderByCreatedAtDesc(userId);

        List<SideBarPromptListDto> dtoList = new ArrayList<>();
        for(Prompt one:list){
            dtoList.add(SideBarPromptListDto.toDto(one));
        }
        return dtoList;
    }

    public SideBarPromptDto checkSideBarDetails(Long promptId){
        Prompt prompt=promptRepository.findById(promptId)
                .orElseThrow(()->new BusinessException(PROMPT_NOT_FOUND));

        List<Answer> answers=prompt.getAnswers();
        List<AnswerDto> dtoList = new ArrayList<>();
        for(Answer answer:answers){
            dtoList.add(AnswerDto.from(answer));
        }

        SideBarPromptDto dto= new SideBarPromptDto(prompt,dtoList);
        return dto;
    }

    public List<PromptListDto> getOptimizedPromptList(Long userId) {
        List<Prompt> prompts = promptRepository.findPromptWithOptimizedPrompt(userId);

        return prompts.stream()
                .map(prompt -> new PromptListDto(
                        prompt.getId(),
                        prompt.getSummary(),
                        prompt.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<PromptListDto> getCrosscheckList(Long userId) {
        List<Prompt> prompts = promptRepository.findPromptsWithAnswersAndScoreNotNull(userId);

        return prompts.stream()
                .map(prompt -> new PromptListDto(
                        prompt.getId(),
                        prompt.getSummary(),
                        prompt.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public OptimizedPromptResultDto getOptimizedPromptResult(Long promptId) {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new RuntimeException("Prompt not found"));

        return new OptimizedPromptResultDto(
                prompt.getId(),
                prompt.getSummary(),
                prompt.getOriginalPrompt(),
                prompt.getOptimizedPrompt()
        );
    }




}
