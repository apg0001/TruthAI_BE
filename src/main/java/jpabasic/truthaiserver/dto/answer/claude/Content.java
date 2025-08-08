package jpabasic.truthaiserver.dto.answer.claude;

import lombok.Data;

@Data
//claude 답변 형식
public class Content {

    private String text;
    private String type;

}
