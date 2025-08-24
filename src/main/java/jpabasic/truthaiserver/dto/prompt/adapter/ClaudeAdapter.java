package jpabasic.truthaiserver.dto.prompt.adapter;

import jpabasic.truthaiserver.dto.prompt.template.BasePromptTemplate;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Map;

@Component
public class ClaudeAdapter extends BasePromptTemplate {


    public static Map<String, Object> createStructuredAnswerTool() {
        return Map.of(
                "name", "get_structured_answer",
                "description", "Return the final answer in a strict schema (answer + sources).",
                "input_schema", Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of(
                                "answer", Map.of(
                                        "type", "string",
                                        "minLength", 1,
                                        "description", "The final answer text."
                                ),
                                "sources", Map.of(
                                        "type", "array",
                                        "minItems", 1,
                                        "description", "Credible sources backing the answer.",
                                        "items", Map.of(
                                                "type", "object",
                                                "additionalProperties", false,
                                                "properties", Map.of(
                                                        "title", Map.of(
                                                                "type", "string",
                                                                "minLength", 1,
                                                                "description", "Title of the source"
                                                        ),
                                                        "url", Map.of(
                                                                "type", "string",
                                                                "format", "uri",
                                                                "description", "URL of the source"
                                                        )
                                                ),
                                                "required", List.of("title", "url")
                                        )
                                )
                        ),
                        "required", List.of("answer", "sources")
                )
        );
    }
    


//    public MessageCreateParams toClaudeStructuredRequest(Message userMsg,String persona,PromptDomain domain){
//        List<String> systemBlocks=new ArrayList<>();
//        systemBlocks.add(systemIdentity(domain,persona));
//        systemBlocks.add(globalGuidelines());
//
//        if (!domainGuidelines().isBlank()) systemBlocks.add(domainGuidelines());
//        if (!fewShotExamples(new HashMap<>()).isBlank()) systemBlocks.add(fewShotExamples(new HashMap<>()));
//
//        //Claude 텍스트 블록 시스템 메시지
//        List<ContentBlockParam> systemContent=systemBlocks.stream()
//                .map(text->ContentBlockParam.ofText(TextBlockParam.builder().text(text).build()))
//                .toList();
//
//        //Claude 사용자 메시지 (user 질문)
//        ContentBlockParam userContent=ContentBlockParam.ofText(
//                TextBlockParam.builder().text(userMsg.getContent()).build()
//        );
//
//        return MessageCreateParams.builder()
//                .model(Model.CLAUDE_3_OPUS) // 또는 SONNET
//                .maxTokens(1024)
//                .addTool(tool)
//                .toolChoice(ToolChoiceTool.builder().name("get_structured_answer").build())
//                .system(SystemPrompt.builder().setContent(systemContent).build())
//                .addUserMessageOfBlockParams(List.of(userContent))
//                .build();
//    }
//
//    Tool tool=Tool.builder()
//            .name("get_structured_answer")
//            .description("Answer the user question using a structured JSON format.")
//            .inputSchema(Tool.InputSchema.builder()
//                    .properties(JsonValue.from(Map.of(
//                            "answer",Map.of(
//                                    "type","string",
//                                    "description","The answer to the user's question"
//                            ),
//                            "sources",Map.of(
//                                    "type","array",
//                                    "description","List of credible sources used in the answer.",
//                                    "items",Map.of(
//                                            "title",Map.of("type","string","description","Title of the source."),
//                                            "url",Map.of("type","string","description","URL of the source.")
//                                    ),
//                                    "required",List.of("title","url")
//                            )
//                    )))
//                    .putAdditionalProperty("required",JsonValue.from(List.of("answer","sources")))
//                    .build())
//            .build();





}
