package jpabasic.truthaiserver.dto.answer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LlmRequestDto {

    private Long userId;

    @Schema(description="model 선택. gpt, claude, gemini 중 선택한 것 들을 리스트로 주세요.")
    @NotBlank(message="모델을 선택해주세요.")
    private List<String> models;

    @NotBlank(message="프롬프트를 작성해주세요.")
    private String question;

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
