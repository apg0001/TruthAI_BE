package jpabasic.truthaiserver.dto.answer;

import jpabasic.truthaiserver.domain.LLMModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LlmAnswerDto {

    private LLMModel llmModel;
    private String answer;

}
