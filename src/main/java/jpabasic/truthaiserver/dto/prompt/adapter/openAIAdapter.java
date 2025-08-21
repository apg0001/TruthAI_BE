package jpabasic.truthaiserver.dto.prompt.adapter;

import jpabasic.truthaiserver.dto.prompt.template.BasePromptTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class openAIAdapter extends BasePromptTemplate {

    public static Map<String, Object> functionSchema() {
        return functionSchema;
    }


    public static Map<String, Object> functionSchema = Map.of(
            "name", "get_structured_answer",
            "description", "Answer a user question in structured format",
            "parameters", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "answer", Map.of(
                                    "type", "string",
                                    "description", "The answer to the user question"
                            ),
                            "sources", Map.of(
                                    "type", "array",
                                    "description", "Credible sources with URLs",
                                    "items", Map.of(
                                            "type", "object",
                                            "properties", Map.of(
                                                    "title", Map.of("type", "string", "description", "Source title"),
                                                    "url", Map.of("type", "string", "description", "Source URL")
                                            ),
                                            "required", List.of("title", "url")
                                    )
                            )
                    ),
                    "required", List.of("answer", "sources")
            )
    );


}
