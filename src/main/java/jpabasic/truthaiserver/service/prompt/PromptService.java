package jpabasic.truthaiserver.service.prompt;

import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.Prompt;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.PromptRepository;
import jpabasic.truthaiserver.repository.UserRepository;
import jpabasic.truthaiserver.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jpabasic.truthaiserver.exception.ErrorMessages;

import java.util.List;

import static jpabasic.truthaiserver.exception.ErrorMessages.PROMPT_GENERATE_ERROR;


@Service
@Slf4j
public class PromptService {

    private final PromptRepository promptRepository;
    private final UserRepository userRepository;
    private final LlmService llmService;
    private final PromptEngine promptEngine;
    private final OptimizedTemplate optimizedTemplate;

    public PromptService(PromptRepository promptRepository, UserRepository userRepository, LlmService llmService, PromptEngine promptEngine, OptimizedTemplate optimizedTemplate) {
        this.promptRepository = promptRepository;
        this.userRepository = userRepository;
        this.llmService = llmService;
        this.promptEngine = promptEngine;
        this.optimizedTemplate = optimizedTemplate;
    }


    //summary 생성해야 (생성형 ai 이용)
    @Transactional
    public void summarize(String text){

    }


    @Transactional
    public void savePrompt(String question, List<LlmAnswerDto> results, User user) {

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


    //프롬프트 내용 요약
    public String summarizePrompts(String prompt){
        return promptEngine.execute("summarize",prompt);
    }

    //최적화 프롬프트 실행(현재는 gpt로만)
    public String optimizingPrompt(LlmRequestDto dto) {
        return promptEngine.execute("optimized",dto);
    }
    
    //최적화된 프롬프트 반환
    public String getOptimizedPrompt(LlmRequestDto dto) {
        return promptEngine.optimizingPrompt(dto);
    }

    @Transactional
    public User searchUser(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()-> new BusinessException(ErrorMessages.USER_NULL_ERROR));
    }
}
