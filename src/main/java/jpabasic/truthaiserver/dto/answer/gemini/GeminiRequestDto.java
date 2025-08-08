package jpabasic.truthaiserver.dto.answer.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequestDto {

    private List<Contents> contents;

    public static GeminiRequestDto fromText(String text) {
        return new GeminiRequestDto(
                List.of(new Contents(List.of(new Part(text))))
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contents {
        private List<Part> parts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class Part {
        private String text;
    }
}
