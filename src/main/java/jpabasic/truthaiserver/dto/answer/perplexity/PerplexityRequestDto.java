package jpabasic.truthaiserver.dto.answer.perplexity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jpabasic.truthaiserver.dto.answer.Message;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PerplexityRequestDto {

    private String model;

    private List<Message> messages;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private Double temperature;

    public PerplexityRequestDto(String message){
        this.model="sonar";
        this.messages=new ArrayList<>();
        messages.add(new Message(message));
        this.maxTokens=500;
        this.temperature=0.0;
    }
}
