package jpabasic.truthaiserver.dto.answer.claude;

import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeRequestDto {
    private String model;
    private int max_tokens; //사용할 수 있는 최대 토큰 수
    private List<ClaudeTextBlock> system;
    private List<ClaudeMessage> messages;




//    public ClaudeRequest(String model, String prompt) {
//        this.model = model;
//        this.messages = new ArrayList<>();
//        this.messages.add(new ClaudeMessage(prompt,"user"));
//        this.max_tokens=1024;
//    }

    //text 덩어리 : globalDomain, system .. 등등을 분리해서 관리하기 쉽게 하기 위해
    public record ClaudeTextBlock(String type,String text) {
        public static ClaudeTextBlock text(String t) {
            return new ClaudeTextBlock("text", t);
        }

        //여러 문자열 -> 텍스트 블록 리스트
        public static List<ClaudeTextBlock> ofTexts(String... texts) {
            if (texts == null || texts.length == 0) {
                return List.of();
            }
            List<ClaudeTextBlock> textBlocks = new ArrayList<>(texts.length);
            for (String t : texts) {
                if (t == null) continue;
                textBlocks.add(text(t));
            }
            return textBlocks;
        }


        public static List<ClaudeTextBlock> ofTexts(List<String> texts){
            return ofTexts(texts.toArray(String[]::new));

        }
    }

    public ClaudeRequestDto(List<ClaudeMessage> messages){
        this.model="claude-3-5-sonnet-20241022";
        this.messages = messages;
        this.max_tokens=1024;
    }


    public ClaudeRequestDto(List<ClaudeTextBlock> systemText, String userText){
        this.system=systemText;
        this.messages = new ArrayList<>();
        this.messages.add(new ClaudeMessage(userText,"user"));
        this.max_tokens=1024;
        this.model="claude-3-5-sonnet-20241022";
    }

    public ClaudeRequestDto(String userText){
        this.system = null;
        this.messages = new ArrayList<>();
        this.messages.add(new ClaudeMessage(userText,"user"));
        this.max_tokens=1024;
        this.model="claude-3-5-sonnet-20241022";
    }

    @Data
    public static class ClaudeMessage {
        private String content;
        private String role;

        public ClaudeMessage(String content, String role) {
            this.content = content;
            this.role = role;
        }

        public static ClaudeRequestDto.ClaudeMessage messageToClaudeMessage(Message message) {
            return new ClaudeRequestDto.ClaudeMessage( message.getContent(),message.getRole());
        }
    }

}





