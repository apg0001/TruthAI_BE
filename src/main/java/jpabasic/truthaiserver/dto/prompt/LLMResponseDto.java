package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.dto.answer.perplexity.PerplexityResponseDto;

import java.util.List;

public record LLMResponseDto(
        String answer,
        List<SourceResponseDto> sources
) {
    public record SourceResponseDto(String title, String url) {}

    public static List<SourceResponseDto> toSourceResponseDto(PerplexityResponseDto dto) {
        return dto.getSearchResults().stream()
                .map(m->new SourceResponseDto(m.getTitle(), m.getUrl()))
                .toList();
    }

    public static LLMResponseDto toLLMResponseDto(PerplexityResponseDto dto) {
        List<SourceResponseDto> sources=toSourceResponseDto(dto);
        String answer=dto.getChoices().get(0).getMessage().getContent();
        return new LLMResponseDto(answer,sources);
    }
}
