package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePromptTemplate {

    public static Map<String, Object> functionSchema() {
        return functionSchema;
    }

    public List<Message> render(Message message, String persona, PromptDomain domain) {
        List<Message> msgs = new ArrayList<>();
        msgs.add(new Message("system", systemIdentity(domain, persona)));
        msgs.add(new Message("user", userContent(message)));
        return executeInternalRender(message, msgs);
    }

    public List<Message> render(Message message) {
        List<Message> msgs = new ArrayList<>();
        return executeInternalRender(message, msgs);
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

    private List<Message> executeInternalRender(Message message, List<Message> msgs) {
        Map<String, Object> vars = new HashMap<>();

        msgs.add(new Message("system", globalGuidelines()));
        String domain = domainGuidelines();
        if (domain != null && !domain.isBlank()) {
            msgs.add(new Message("system", domain));
        }
        String fewshot = fewShotExamples(vars);
        if (fewshot != null && !fewshot.isBlank()) {
            msgs.add(new Message("system", fewshot));
        }


        return msgs;
    }

    protected String systemIdentity(PromptDomain domain, String persona) {
        return String.format("You are an expert %s advisor and educator for %s.", domain, persona);
    }

    protected String globalGuidelines() {
        return """
                final answer should be submbitted onlly by "get_structured_answer" tool.
                No free text.
                """;
    }

    protected String domainGuidelines() {
        return "";
    }

    protected String fewShotExamples(Map<String, Object> vars) {
        return """
                Example 1:
                {
                  "answer": "비타민 C는 감기 예방 효과가 일관되게 입증되지는 않았습니다. 다만 증상 기간 단축은 일부 보고됩니다.",
                  "sources": [
                    { "title": "Cochrane Review (2013, updated)", "url": "https://www.cochranelibrary.com/cdsr/doi/10.1002/14651858.CD000980.pub4/full" },
                    { "title": "CDC - Common Cold", "url": "https://www.cdc.gov/common-cold/" }
                  ]
                }

                Example 2:
                {
                  "answer": "비트코인은 가명성에 가깝고 온체인 분석으로 지갑-사용자 연계가 가능합니다.",
                  "sources": [
                    { "title": "Chainalysis Industry Reports", "url": "https://www.chainalysis.com/reports/" },
                    { "title": "Bitcoin and Cryptocurrency Technologies", "url": "https://press.princeton.edu/books/hardcover/9780691171692/bitcoin-and-cryptocurrency-technologies" }
                  ]
                }
                """;
    }

    protected String userContent(Message message) {
        String text = message.getContent();
        return """
                Task:
                - Answer the question strictly as the JSON shape above.
                - Provide at least 2 credible sources with valid URLs.
                - No markdown, no explanation.
                - Only JSON.

                Question: ```%s```
                """.formatted(text);
    }

    public String key() {
        return "optimized";
    }
}
