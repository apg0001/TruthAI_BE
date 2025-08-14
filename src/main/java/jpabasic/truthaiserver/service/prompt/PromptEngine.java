package jpabasic.truthaiserver.service.prompt;

import jpabasic.truthaiserver.common.prompt.PromptRegistry;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.PromptTemplate;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptEngine {

    private final LlmService llmService;
    private final PromptRegistry registry;
    private final OptimizedTemplate optimizedTemplate;


    //persona 없는 경우
    public String execute(String templateKey,String question){
        return getOptimizedAnswer(templateKey,new Message(question),null,null);
    }

    //persona 있는 경우
    //최적화 프롬프트 -> 응답까지 생성
    public String execute(String templateKey,LlmRequestDto request){
       return getOptimizedAnswer(templateKey,new Message(request.getQuestion()),request.getPersona(),request.getDomain());
    }

    //최적화 프롬프트 반환(String type)
    public String optimizingPrompt(LlmRequestDto request){
        String domain=request.getDomain();
        String persona=request.getPersona();
        Message message=new Message(request.getQuestion());

        return optimizedTemplate.getOptimizedPrompt(domain,persona,message);
    }

    //최적화 프롬프트 반환(List<Message>)
    private List<Message> executeInternal(String templateKey, Message message, @Nullable String persona,@Nullable String domain){
        PromptTemplate template=registry.getByKey(templateKey);
        if(template==null){
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }
        return template.render(message,persona,domain); //기능에 맞는 프롬프트 찾아서 실행

    }

    //최적화 프롬프트 -> 생성형 ai한테 응답까지 받기
    private String getOptimizedAnswer(String templateKey, Message message, @Nullable String persona,@Nullable String domain){
        List<Message> result=executeInternal(templateKey,message,persona,domain);
        return llmService.createGptAnswerWithPrompt(result);
    }

}
