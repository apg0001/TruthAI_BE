package jpabasic.truthaiserver.dto.answer.claude;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResponse {

    private String id;                // ex) "msg_..."
    private String type;              // "message"
    private String role;              // "assistant"
    private String model;             // ex) "claude-3-5-sonnet-20241022"
    private List<Content> content;

    @JsonProperty("stop_reason")
    private String stopReason;        // "end_turn" | "max_tokens" | "tool_use" | "stop_sequence"
    @JsonProperty("stop_sequence")
    private String stopSequence;

    private Usage usage;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("input_tokens")
        private int inputTokens;
        @JsonProperty("output_tokens")
        private int outputTokens;
        // 필요 시 cache 관련 토큰 필드가 추가될 수 있어 unknown 허용
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String type;  // "text" | "tool_use"
        private String text;  // when type == "text"

        @JsonUnwrapped // id/name/input을 부모 로 펼쳐서 매핑
        private ToolUse toolUse; // when type == "tool_use"

        @JsonUnwrapped
        private ToolResult toolResult;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolUse {
        private String id;                 // tool_use 블록 ID
        private String name;               // 호출된 도구명 (ex: "get_structured_answer")
        private Map<String, Object> input; // 도구 입력(JSON 객체)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolResult {
        @JsonProperty("tool_use_id")
        private String toolUseId;
        private Object content;
    }
}
