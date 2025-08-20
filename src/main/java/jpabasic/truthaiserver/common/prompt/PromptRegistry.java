package jpabasic.truthaiserver.common.prompt;

import jpabasic.truthaiserver.dto.prompt.BasePromptTemplate;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PromptRegistry {

    public final Map<String, BasePromptTemplate> byKey=new HashMap<String,BasePromptTemplate>();

    public PromptRegistry(List<BasePromptTemplate> templates) {
        for(BasePromptTemplate template : templates) {
            byKey.put(template.key(), template);
        }
    }

    public BasePromptTemplate getByKey(String key) {
        BasePromptTemplate template = byKey.get(key);
        if(template == null) {
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }
        return template;
    }
}
