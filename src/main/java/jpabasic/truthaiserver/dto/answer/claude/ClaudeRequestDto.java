package jpabasic.truthaiserver.dto.answer.claude;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeRequestDto {
    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    /** 권장: system은 String */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String system;

    /** tools / tool_choice */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Map<String,Object>> tools;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("tool_choice")
    private Map<String,Object> toolChoice;

    /** messages */
    private List<Message> messages;

    /** 편의 생성자 */
    public ClaudeRequestDto(String model, String system, List<Map<String,Object>> tools, List<Message> messages) {
        this.model = model;
        this.maxTokens = 1024;
        this.system = system;
        this.tools = tools;
        this.messages = messages;
    }

    /** ⚠️ FIX: 단일 질문용 생성자 */
    public ClaudeRequestDto(String question){
        this.model = "claude-3-5-sonnet-20241022";
        this.maxTokens = 1024;
        this.system = null;
        this.tools = null;
        this.messages = List.of(Message.userText(question)); // ← List로 감싸기
    }

    /** message: role + content[] */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;               // "user" | "assistant"
        private List<Content> content;     // 항상 배열!

        public static Message userText(String text) {
            return new Message("user", List.of(Content.text(text)));
        }
        public static Message assistantToolUse(String id, String name, Map<String,Object> input) {
            return new Message("assistant", List.of(Content.toolUse(id, name, input)));
        }
        public static Message userToolResult(String toolUseId, Object content) {
            return new Message("user", List.of(Content.toolResult(toolUseId, content)));
        }
    }

    /** content block: text | tool_use | tool_result
     *  ⚠️ tool_use/tool_result는 “동레벨 필드”로 직렬화해야 함 (중첩 금지)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {
        private String type; // "text" | "tool_use" | "tool_result"

        // type = "text"
        private String text;

        // type = "tool_use"
        private String id;                       // tool_use id (top-level)
        private String name;                     // tool name (top-level)
        private Map<String,Object> input;        // tool input (top-level)

        // type = "tool_result"
        @JsonProperty("tool_use_id")
        private String toolUseId;                // (top-level)
        private Object content;                  // String or JSON object (top-level)

        // factories
        public static Content text(String t) {
            Content c = new Content();
            c.type = "text";
            c.text = t;
            return c;
        }
        public static Content toolUse(String id, String name, Map<String,Object> input) {
            Content c = new Content();
            c.type = "tool_use";
            c.id = id;
            c.name = name;
            c.input = input;
            return c;
        }
        public static Content toolResult(String toolUseId, Object content) {
            Content c = new Content();
            c.type = "tool_result";
            c.toolUseId = toolUseId;
            c.content = content;
            return c;
        }
    }

    /** tool_choice 헬퍼 (선택) */
    public static Map<String,Object> toolChoiceAuto() { return Map.of("type", "auto"); }
    public static Map<String,Object> toolChoiceAny()  { return Map.of("type", "any"); }
    public static Map<String,Object> toolChoiceForce(String name) {
        return Map.of("type", "tool", "name", name);
    }
}
