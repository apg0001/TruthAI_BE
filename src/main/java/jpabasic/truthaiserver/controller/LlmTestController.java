package jpabasic.truthaiserver.controller;

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

    private final WebClient openAiWebClient;
    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;


    private final WebClient.Builder webClientBuilder;

    public LlmTestController(WebClient.Builder webClientBuilder,WebClient openAiWebClient) {
        this.webClientBuilder = webClientBuilder;
        this.openAiWebClient = openAiWebClient;
    }


    @GetMapping("/chatgpt-test")
    public String chatgptTest(@RequestParam(name="prompt")String prompt) {

        ChatGptRequest request=new ChatGptRequest(model,prompt);

        //webClient로 OpenAI로 호출
        ChatGptResponse chatGptResponse=openAiWebClient.post()
                .uri(apiUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGptResponse.class)
                .block();
        return chatGptResponse.getChoices().get(0).getMessage().getContent();
    }
}
