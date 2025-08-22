package jpabasic.truthaiserver.dto.prompt.sidebar;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.Prompt;
import jpabasic.truthaiserver.dto.answer.AnswerDto;

import java.util.List;

public record SideBarPromptDto (
        Long promptId, String originalPrompt, String optimizedPrompt,
        String summary, List<AnswerDto> answerDto
        ){

        public SideBarPromptDto(Prompt prompt,List<AnswerDto> dtoList){
                this(prompt.getId(),prompt.getOriginalPrompt(),
                        prompt.getOptimizedPrompt(),prompt.getSummary(),dtoList);

        }
}
