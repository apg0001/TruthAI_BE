package jpabasic.truthaiserver.controller;

import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequest;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeResponse;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/llm")
public class LlmTestController {

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String gptUrl;

    @Value("${claude.api.url}")
    private String claudeApiUrl;

    @Value("${claude.model}")
    private String claudeModel;


    private final WebClient.Builder webClientBuilder;
    private final WebClient openAiWebClient;
    private final WebClient claudeClient;

    public LlmTestController(WebClient.Builder webClientBuilder,WebClient openAiWebClient,WebClient claudeClient) {
        this.webClientBuilder = webClientBuilder;
        this.openAiWebClient = openAiWebClient;
        this.claudeClient=claudeClient;
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
}
