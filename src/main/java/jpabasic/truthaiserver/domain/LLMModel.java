package jpabasic.truthaiserver.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;

import java.util.Arrays;

public enum LLMModel {
    GPT, CLAUDE, GEMINI, PERPLEXITY;

    @JsonCreator
    public static LLMModel fromString(final String value) {
        return Arrays.stream(LLMModel.values())
                .filter(e->e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(()->new BusinessException(ErrorMessages.ENUM_ERROR));
    }

}