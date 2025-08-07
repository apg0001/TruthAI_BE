package jpabasic.truthaiserver.dto.answer.openai;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Message {

    private String role;
    private String content;
}
