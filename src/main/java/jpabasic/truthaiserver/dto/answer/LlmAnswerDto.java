package jpabasic.truthaiserver.dto.answer;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@Builder
public class LlmAnswerDto {

    private LLMModel llmModel;
    private String answer;

    public Answer toEntity() {
        return new Answer(this.llmModel, this.answer);
    }


    public LlmAnswerDto(LLMModel llmModel, String answer){
        this.llmModel = llmModel;
        this.answer = answer;
    }

}
