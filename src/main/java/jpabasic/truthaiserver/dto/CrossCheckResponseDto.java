package jpabasic.truthaiserver.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CrossCheckResponseDto {
    private Long promptId;
    private List<LLMResultDto> llmResultDtoList;
}
