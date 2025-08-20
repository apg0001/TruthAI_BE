package jpabasic.truthaiserver.dto.prompt;

import java.util.List;

public record LLMResponseDto(
        String answer,
        List<SourceResponseDto> sources
) {
    public record SourceResponseDto(String title, String url) {}
}
