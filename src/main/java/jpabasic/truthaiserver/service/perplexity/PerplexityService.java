package jpabasic.truthaiserver.service.perplexity;

import com.fasterxml.jackson.core.JsonProcessingException;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.perplexity.PerplexityRequestDto;
import jpabasic.truthaiserver.dto.answer.perplexity.PerplexityResponseDto;
import jpabasic.truthaiserver.dto.prompt.LLMResponseDto;
import jpabasic.truthaiserver.service.LlmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@Slf4j
public class PerplexityService {

    private final WebClient perplexityClient;

    public PerplexityService(WebClient perplexityClient) {
        this.perplexityClient = perplexityClient;
    }

    public LLMResponseDto structuredWithPerplexity(
            List<Message> messageList
    ){
        PerplexityRequestDto request = new PerplexityRequestDto(messageList);
        System.out.println("🥔 request:"+request.getMessages().toString());

        return organizeAnswer(request);
    }

    public LLMResponseDto organizeAnswer(PerplexityRequestDto request){
        PerplexityResponseDto response=perplexityClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PerplexityResponseDto.class)
                .block();

        return LLMResponseDto.toLLMResponseDto(response);

    }
}
