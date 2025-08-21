package jpabasic.truthaiserver.dto.answer;

import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;

import java.util.List;
import java.util.stream.Collectors;

public record LlmNoPromptRequestDto (List<String> models,String question){

    public List<LLMModel> toModelEnums(){
        return models.stream()
                .map(model->{
                    try{
                        return LLMModel.valueOf(model.toUpperCase());
                    }catch(IllegalArgumentException e){
                        throw new BusinessException(ErrorMessages.ENUM_ERROR);
                    }
                })
                .collect(Collectors.toList());
    }
}
