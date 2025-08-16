package jpabasic.truthaiserver.service.prompt;

import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.domain.Prompt;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.AnswerRepository;
import jpabasic.truthaiserver.repository.PromptRepository;
import jpabasic.truthaiserver.repository.UserRepository;
import jpabasic.truthaiserver.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jpabasic.truthaiserver.exception.ErrorMessages;

import java.util.ArrayList;
import java.util.List;

import static jpabasic.truthaiserver.domain.LLMModel.GPT;
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


    @Transactional
    public Long saveOptimizedPrompt(String optimizedPrompt,Long promptId){
        Prompt prompt=promptRepository.findById(promptId)
                .orElseThrow(()->new BusinessException(PROMPT_NOT_FOUND));
        prompt.optimize(optimizedPrompt);
        System.out.println("✅promptId"+prompt.getId());
        return prompt.getId();
    }


    //프롬프트 내용 요약
    public String summarizePrompts(String prompt){
        return promptEngine.execute("summarize",prompt);
    }

    //최적화 프롬프트 실행(현재는 gpt로만)
    public String optimizingPrompt(LlmRequestDto dto,User user,Long promptId) {
        String answer= promptEngine.execute("optimized",dto);
        LLMModel model=GPT;
        saveAnswer(answer,user,promptId,model);
        return answer;
    }
    
    //최적화된 프롬프트 반환
    public String getOptimizedPrompt(LlmRequestDto dto,Long promptId) {
//        Long promptId=saveOriginalPrompt(dto,user);
        String optimizedPrompt=promptEngine.optimizingPrompt(dto);
        saveOptimizedPrompt(optimizedPrompt,promptId);
        return optimizedPrompt;
    }

    //gpt 응답 저장
    @Transactional
    public void saveAnswer(String content,User user,Long promptId,LLMModel model) {

        log.info("✅User:{}",user);

        Prompt prompt=promptRepository.findById(promptId)
                .orElseThrow(()->new BusinessException(PROMPT_NOT_FOUND));

        //Answer entity 생성
        Answer answer=new Answer(content,model,prompt,user);
        answerRepository.save(answer);

        // Prompt entity와 매핑
        prompt.addAnswer(answer);

    }


}
