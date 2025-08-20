package jpabasic.truthaiserver.dto.prompt;


import jpabasic.truthaiserver.domain.LLMModel;

public record OptimizedResponseDto (LLMModel model,PromptAnswerDto promptAnswerDto) {

    public OptimizedResponseDto (LLMModel model,PromptAnswerDto promptAnswerDto) {
        this.model = model;
        this.promptAnswerDto = promptAnswerDto;
    }
}
