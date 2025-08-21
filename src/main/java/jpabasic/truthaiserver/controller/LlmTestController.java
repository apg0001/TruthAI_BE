package jpabasic.truthaiserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiResponseDto;
import jpabasic.truthaiserver.dto.answer.perplexity.PerplexityRequestDto;
import jpabasic.truthaiserver.dto.answer.perplexity.PerplexityResponseDto;
import jpabasic.truthaiserver.dto.prompt.LLMResponseDto;
import jpabasic.truthaiserver.service.prompt.PromptEngine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/llm")
@Tag(name="llm 답변 테스트 api",description = "연동 용이 아닌, 백엔드 테스트 용입니다.")
public class LlmTestController {

    private final WebClient perplexityClient;
    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String gptUrl;

    @Value("${claude.api.url}")
    private String claudeApiUrl;

    @Value("${claude.model}")
    private String claudeModel;

    @Value("${gemini.api.key}")
    private String geminiApiKey;


    private final WebClient.Builder webClientBuilder;
    private final WebClient openAiWebClient;
    private final WebClient claudeClient;
    private final WebClient geminiClient;
    private final PromptEngine promptEngine;


    public LlmTestController(WebClient.Builder webClientBuilder, WebClient openAiWebClient, WebClient claudeClient, WebClient geminiClient, PromptEngine promptEngine, WebClient perplexityClient) {
        this.webClientBuilder = webClientBuilder;
        this.openAiWebClient = openAiWebClient;
        this.claudeClient=claudeClient;
        this.geminiClient=geminiClient;
        this.promptEngine=promptEngine;
        this.perplexityClient = perplexityClient;
    }


    @GetMapping("/chatgpt-test")
    public ResponseEntity<LLMResponseDto> chatgptTest() throws JsonProcessingException {

//        ChatGptRequest request=new ChatGptRequest(model,prompt);

        //webClient로 OpenAI로 호출
//        ChatGptResponse chatGptResponse=openAiWebClient.post()
//                .uri(gptUrl)
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(ChatGptResponse.class)
//                .block();
//        return chatGptResponse.getChoices().get(0).getMessage().getContent();

        LLMResponseDto dto=promptEngine.getStructuredAnswerByGpt("optimized", new Message("스티브 잡스가 누구야"), "22살 대학생", PromptDomain.SCIENCE);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/claude-test")
    public ResponseEntity<LLMResponseDto> claudeTest() throws JsonProcessingException{

            LLMResponseDto dto=promptEngine.getStructuredAnswerByClaude("optimized",new Message("스티브 잡스가 누구야"),"22살 대학생",PromptDomain.SCIENCE);
            return ResponseEntity.ok(dto);
    }

    @GetMapping("/gemini-test")
    public String geminiTest(@RequestParam(name="prompt")String prompt) {

        GeminiRequestDto request = GeminiRequestDto.fromText(null,prompt);

        //WebClient로 gemini 호출
        GeminiResponseDto response = geminiClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .block();
        return response
                .getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getText();
    }

    @GetMapping("/perplexity-test")
    public String perplexityTest(@RequestParam(name="prompt")String prompt) {

        PerplexityRequestDto request=new PerplexityRequestDto(prompt);
        System.out.println("✅ perplexity request:"+request);

        PerplexityResponseDto response=perplexityClient.post()
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PerplexityResponseDto.class)
                .block();


        return response.getChoices().get(0).getMessage().getContent();
    }
}
