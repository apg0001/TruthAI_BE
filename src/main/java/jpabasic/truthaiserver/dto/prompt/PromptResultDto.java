package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.dto.answer.AnswerDto;
import jpabasic.truthaiserver.dto.sources.SourcesDto;

import java.util.List;

public record PromptResultDto ( AnswerDto answer, List<SourcesDto> sources) {


}
