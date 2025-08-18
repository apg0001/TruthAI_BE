package jpabasic.truthaiserver.service.prompt;

import jpabasic.truthaiserver.common.prompt.PromptRegistry;
import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.prompt.BasePromptTemplate;
import jpabasic.truthaiserver.dto.prompt.ClaudeAdapter;
import jpabasic.truthaiserver.dto.prompt.GeminiAdapter;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptEngine {

    private final LlmService llmService;
    private final PromptRegistry registry;
    private final OptimizedTemplate optimizedTemplate;
    private final GeminiAdapter geminiAdapter;
    private final ClaudeAdapter claudeAdapter;


    //persona 없는 경우
    public String execute(String templateKey,String question){
        return getOptimizedAnswerByGpt(templateKey,new Message(question),null,null);
    }


    //최적화 프롬프트 반환(String type)
    public String optimizingPrompt(LlmRequestDto request){
        PromptDomain domain=request.getPromptDomain();
//        PromptDomain promptDomain=PromptDomain.nameOf(domain);
        String persona=request.getPersona();
        Message message=new Message(request.getQuestion());

        return optimizedTemplate.getOptimizedPrompt(domain,persona,message);
    }

    //최적화 프롬프트 반환(List<Message>) -> 실행 까쥐~
    public List<Message> executeInternal(String templateKey, Message message, @Nullable String persona,@Nullable PromptDomain domain){
        BasePromptTemplate template=registry.getByKey(templateKey);
        if(template==null){
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }
        return template.render(message,persona,domain); //기능에 맞는 프롬프트 찾아서 실행


    }

    //claude 실행

    /**
     *
     * @param templateKey = "optimized"
     * @param message = "유저 질문- user:"~?" "
     * @param persona
     * @param domain
     * @return
     */
    public String getOptimizedAnswerByClaude(String templateKey, Message message, @Nullable String persona,@Nullable PromptDomain domain){
        BasePromptTemplate template=registry.getByKey(templateKey);
        if(template==null){
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }
        ClaudeRequestDto dto=claudeAdapter.toClaudeRequest(message,persona,domain);
        return llmService.createClaudeAnswerWithPrompt(dto);
    }

    //gpt 실행
    public String getOptimizedAnswerByGpt(String templateKey, Message message, @Nullable String persona,@Nullable PromptDomain domain){
        List<Message> result=executeInternal(templateKey,message,persona,domain);
        return llmService.createGptAnswerWithPrompt(result);
    }

    //gemini 실행
    public String getOptimizedAnswerByGemini(String templateKey, Message message, @Nullable String persona,@Nullable PromptDomain domain){
        //"optimized" 프롬프트 가져오기
        BasePromptTemplate template=registry.getByKey(templateKey);
        if(template==null){
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }

        GeminiRequestDto dto=geminiAdapter.toGeminiRequest(message,persona,domain);
        return llmService.createGeminiAnswerWithPrompt(dto);
    }



    //optimized prompt 반환
    List<Message> getOptimizedPrompt(String templateKey, LlmRequestDto request){
        Message message=new Message(request.getQuestion());
        String persona=request.getPersona();
        PromptDomain domain=request.getPromptDomain();

        List<Message> result=executeInternal(templateKey,message,persona,domain);
        return result;
    }

}
