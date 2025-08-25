package jpabasic.truthaiserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequestDto;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeResponseDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiResponseDto;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptResponse;
import jpabasic.truthaiserver.dto.answer.perplexity.PerplexityRequestDto;
import jpabasic.truthaiserver.dto.answer.perplexity.PerplexityResponseDto;
import jpabasic.truthaiserver.dto.prompt.*;
import jpabasic.truthaiserver.dto.prompt.adapter.ClaudeAdapter;
import jpabasic.truthaiserver.dto.prompt.adapter.openAIAdapter;
import jpabasic.truthaiserver.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static jpabasic.truthaiserver.exception.ErrorMessages.*;

@Service
@Slf4j
public class LlmService {

    private final jpabasic.truthaiserver.dto.prompt.adapter.openAIAdapter openAIAdapter;


    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${claude.api.url}")
    private String claudeApiUrl;

    private final WebClient.Builder webClientBuilder;

    private final WebClient claudeClient;
    private final WebClient geminiClient;
    private final WebClient perplexityClient;
    private final ClaudeAdapter claudeAdapter;


    public LlmService(WebClient.Builder webClientBuilder, WebClient claudeClient, WebClient geminiClient, WebClient perplexityClient, ClaudeAdapter claudeAdapter, openAIAdapter openAIAdapter) {
        this.webClientBuilder = webClientBuilder;

        this.claudeClient = claudeClient;
        this.geminiClient = geminiClient;
        this.perplexityClient = perplexityClient;
        this.claudeAdapter = claudeAdapter;
        this.openAIAdapter = openAIAdapter;
    }







    public String createGeminiAnswer(String question) {
        GeminiRequestDto request = GeminiRequestDto.send(question);
        System.out.println("🏃GeminiRequestDto:" + request);
        GeminiResponseDto answer=geminiClient(request);

        String result=answer.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getText();
        return result;
    }

    public String createPerplexityAnswer(String question){
        PerplexityRequestDto request=new PerplexityRequestDto(question);
        return perplexityClient(request);
    }

















    public GeminiResponseDto geminiClient(GeminiRequestDto request) {

        GeminiResponseDto response = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-goog-api-key", geminiApiKey) // ✅ 헤더 방식으로 키 전달
                .build()
                .post()
                .uri("/models/gemini-2.0-flash:generateContent") // ✅ key는 queryParam 안 써도 됨
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .block();

        return response;

    }

    public String perplexityClient(PerplexityRequestDto request) {

        PerplexityResponseDto response=perplexityClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PerplexityResponseDto.class)
                .block();
        return response.getChoices().get(0).getMessage().getContent();
    }
}



