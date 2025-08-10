package jpabasic.truthaiserver.dto.answer.gemini;

import lombok.Data;

import java.util.List;

@Data
public class GeminiResponseDto {

    private Candidate candidates;

    @Data
    public static class Candidate {
        private Content content;
    }

    @Data
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part{
        private String text;
    }
}
