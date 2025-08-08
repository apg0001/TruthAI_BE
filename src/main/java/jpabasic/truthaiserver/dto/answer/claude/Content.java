package jpabasic.truthaiserver.dto.answer.claude;

import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import lombok.Data;

import java.util.List;

@Data
//claude 답변 형식
public class Content {

    private String text;
    private String type;


}
