package jpabasic.truthaiserver.dto.answer.claude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeRequest {

    private int max_tokens; //사용할 수 있는 최대 토큰 수
    private List<ClaudeMessage> messages;
    private String model;



    public ClaudeRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new ClaudeMessage(prompt,"user"));
        this.max_tokens=1024;
    }

    @Data
    public static class ClaudeMessage {
        private String content;
        private String role;

        public ClaudeMessage(String content, String role) {
            this.content = content;
            this.role = role;
        }
    }

}





