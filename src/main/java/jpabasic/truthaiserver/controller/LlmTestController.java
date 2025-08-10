package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequest;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeResponse;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiResponseDto;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/llm")
@Tag(name="llm 답변 테스트 api",description = "연동 용이 아닌, 백엔드 테스트 용입니다.")
public class LlmTestController {

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

    public LlmTestController(WebClient.Builder webClientBuilder,WebClient openAiWebClient,WebClient claudeClient,WebClient geminiClient) {
        this.webClientBuilder = webClientBuilder;
        this.openAiWebClient = openAiWebClient;
        this.claudeClient=claudeClient;
        this.geminiClient=geminiClient;
    }


    @GetMapping("/chatgpt-test")
    public String chatgptTest(@RequestParam(name="prompt")String prompt) {

        ChatGptRequest request=new ChatGptRequest(model,prompt);

        //webClient로 OpenAI로 호출
        ChatGptResponse chatGptResponse=openAiWebClient.post()
                .uri(gptUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGptResponse.class)
                .block();
        return chatGptResponse.getChoices().get(0).getMessage().getContent();
    }

    @GetMapping("/claude-test")
    public String claudeTest(@RequestParam(name="prompt")String prompt) {

        ClaudeRequest request=new ClaudeRequest(claudeModel,prompt);

        //WebClient로 ClaudeAI로 호출
        ClaudeResponse claudeResponse=claudeClient.post()
                .uri("")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .block();
        return claudeResponse.getContent().get(0).getText();
    }

    @GetMapping("/gemini-test")
    public String geminiTest(@RequestParam(name="prompt")String prompt) {

        GeminiRequestDto request = GeminiRequestDto.fromText(prompt);

        //WebClient로 gemini 호출
        GeminiResponseDto response = geminiClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponseDto.class)
                .block();
        return response
                .getCandidates()
                .getContent()
                .getParts().get(0)
                .getText();
    }
}
