package jpabasic.truthaiserver.dto.answer.claude;

// Anthropic Messages 공용 DTO
import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnthMessageResp {
    private String id;
    private String type; // "message"
    private String role; // "assistant"
    private List<Content> content;
    @JsonProperty("stop_reason")
    private String stopReason;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private String type; // "text" | "tool_use"
        private String text; // when type=text
        @JsonProperty("tool_use")
        private ToolUse toolUse; // when type=tool_use
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolUse {
        private String id;          // tool_use_id
        private String name;        // tool name (e.g., "get_weather")
        private Map<String,Object> input; // arguments object
    }
}



