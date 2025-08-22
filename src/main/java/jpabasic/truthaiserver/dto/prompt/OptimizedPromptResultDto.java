package jpabasic.truthaiserver.dto.prompt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OptimizedPromptResultDto {
    private Long id;
    private String Summary;
    private String originalPrompt;
    private String optimizedPrompt;
}
