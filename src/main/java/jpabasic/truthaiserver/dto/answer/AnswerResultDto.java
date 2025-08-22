package jpabasic.truthaiserver.dto.answer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AnswerResultDto {

    private Long promptId;
    private List<LlmAnswerDto> llmAnswerDto;
}
