package jpabasic.truthaiserver.dto.answer.claude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaudeResponse {

    private List<Content> content;
    private String id;

    private String stop_reason; //답변 문장이 중간에 끊긴 경우 원인
    private String type; //message OR error
    private Usage usage;

    @Data
    public static class Usage{
        public int input_tokens;
        public int output_tokens;
    }


}
