package jpabasic.truthaiserver.dto.crosscheck;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrossCheckResponseDto {
    private String coreTitle;
    private String coreStatement;
    private CrossCheckModelDto gpt;
    private CrossCheckModelDto claude;
    private CrossCheckModelDto gemini;
    private CrossCheckModelDto perplexity;
}
