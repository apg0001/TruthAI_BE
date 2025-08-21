package jpabasic.truthaiserver.dto.answer.perplexity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jpabasic.truthaiserver.dto.answer.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true) //불필요한 필드 무시 설정
public class PerplexityResponseDto {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;
    }


}
