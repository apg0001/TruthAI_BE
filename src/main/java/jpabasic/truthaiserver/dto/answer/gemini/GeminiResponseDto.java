package jpabasic.truthaiserver.dto.answer.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiResponseDto {
    private List<Candidate> candidates;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Candidate {
        private Content content;
        // 필요 시: finishReason, safetyRatings 등 추가
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String role;            // "model"
        private List<Part> parts;       // [{text: "..."}]
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }
}

