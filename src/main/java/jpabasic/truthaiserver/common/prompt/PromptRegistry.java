package jpabasic.truthaiserver.common.prompt;

import jpabasic.truthaiserver.dto.prompt.PromptTemplate;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PromptRegistry {

    public final Map<String, PromptTemplate> byKey=new HashMap<String,PromptTemplate>();

    public PromptRegistry(List<PromptTemplate> templates) {
        for(PromptTemplate template : templates) {
            byKey.put(template.key(), template);
        }
    }

    public PromptTemplate getByKey(String key) {
        PromptTemplate template = byKey.get(key);
        if(template == null) {
            throw new BusinessException(ErrorMessages.PROMPT_NULL_ERROR);
        }
        return template;
    }
}
