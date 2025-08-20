package jpabasic.truthaiserver.dto.sources;

import jpabasic.truthaiserver.domain.Source;

import javax.annotation.Nullable;

public record SourcesDto(
        Long id, String sourceUrl, String sourceTitle) {

    public static SourcesDto toDto(Source source){
        return new SourcesDto(
                source.getId(),source.getSourceUrl(),source.getSourceTitle()

        );
    }
}
