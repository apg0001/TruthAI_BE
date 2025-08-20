package jpabasic.truthaiserver.dto.answer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class Message {

    private String role;
    private String content;

    //assistant가 function call 방식으로 응답할 때 (없을 수도 있음)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FunctionCall function_call;

    public Message(String content) {
        this.role="user";
        this.content = content;
    }

    public Message(String role, String s) {
        this.role=role;
        this.content = s;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FunctionCall {
        private String name;
        private String arguments;
    }



}
