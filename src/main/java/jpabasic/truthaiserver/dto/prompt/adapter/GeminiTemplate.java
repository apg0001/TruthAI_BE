package jpabasic.truthaiserver.dto.prompt.adapter;


import jpabasic.truthaiserver.dto.prompt.template.BasePromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GeminiTemplate extends BasePromptTemplate {

    @Override
    public String globalGuidelines(){
        return null;
    }

    @Override
    public String fewShotExamples(Map<String,Object> vars){
        return null;
    }

    @Override
    public String key() {return "gemini";}



}
