package jpabasic.truthaiserver.service.prompt;

import jpabasic.truthaiserver.common.prompt.PromptRegistry;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.PromptTemplate;
import jpabasic.truthaiserver.service.LlmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptEngine {

    private final LlmService llmService;
    private final PromptRegistry registry;

    public String execute(String templateKey,String question){
        Message message=new Message(question);

        PromptTemplate template=registry.getByKey(templateKey);
        List<Message> result=template.render(message); //기능에 맞는 프롬프트 찾아서 실행

        return llmService.createGptAnswerWithPrompt(result);
    }
}
