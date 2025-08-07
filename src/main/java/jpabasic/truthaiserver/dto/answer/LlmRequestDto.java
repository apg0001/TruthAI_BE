package jpabasic.truthaiserver.dto.answer;

import jpabasic.truthaiserver.domain.LLMModel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class LlmRequestDto {

    private List<LLMModel> models;
    private String question;

}
