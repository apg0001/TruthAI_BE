package jpabasic.truthaiserver.dto.answer;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.domain.Source;
import jpabasic.truthaiserver.dto.sources.SourcesDto;

public record AnswerDto (Long id, LLMModel model,String content){

    public static AnswerDto from(Answer answer) {
        if (answer == null) return null; // 원하면 IllegalArgumentException 던지도록 변경

//        Long claimId = (answer.getPrompt() != null) ? source.getClaim().getId() : null;
        Long folderId = (answer.getFolder() != null) ? answer.getFolder().getId() : null;

        return new AnswerDto(
                answer.getId(),
                answer.getModel(),
                answer.getContent()

        );
    }
}
