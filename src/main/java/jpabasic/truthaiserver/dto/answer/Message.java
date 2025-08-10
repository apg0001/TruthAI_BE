package jpabasic.truthaiserver.dto.answer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Message {

    private String role;
    private String content;

    public Message(String content) {
        this.role="user";
        this.content = content;
    }

}
