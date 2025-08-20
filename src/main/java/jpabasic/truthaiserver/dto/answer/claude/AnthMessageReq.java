package jpabasic.truthaiserver.dto.answer.claude;

// 요청에 쓰는 구조
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AnthMessageReq {
    private String model;
    @JsonProperty("max_tokens")
    private int maxTokens;
    private java.util.List<Tool> tools;
    private java.util.List<Message> messages;

    @Data @Builder
    public static class Tool {
        private String name;
        private String description;
        @JsonProperty("input_schema")
        private Map<String,Object> inputSchema; // JSON Schema (Map 형태가 편함)
    }

    @Data @Builder
    public static class Message {
        private String role; // "user" | "assistant"
        private java.util.List<Content> content;
    }

    @Data @Builder
    public static class Content {
        private String type;             // "text" | "tool_use" | "tool_result"
        private String text;             // for "text"
        @JsonProperty("tool_use")
        private ToolUse toolUse;         // for "tool_use"
        @JsonProperty("tool_result")
        private ToolResult toolResult;   // for "tool_result"
    }

    @Data @Builder
    public static class ToolUse {
        private String id;
        private String name;
        private Map<String,Object> input;
    }

    @Data @Builder
    public static class ToolResult {
        @JsonProperty("tool_use_id")
        private String toolUseId;
        private Object content; // String or structured JSON
    }
}
