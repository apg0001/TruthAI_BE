package jpabasic.truthaiserver.service.gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptResponse;
import jpabasic.truthaiserver.dto.prompt.LLMResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GptService {

    @Value("${openai.api.url}")
    private String gptUrl;

    private final WebClient openAiWebClient;

    public GptService(WebClient openAiWebClient) {
        this.openAiWebClient = openAiWebClient;
    }

    public String createGptAnswer(String question) {
        ChatGptRequest request = new ChatGptRequest("gpt-3.5-turbo", question);
        return gptClient(request);
    }

    public String createGptAnswerWithPrompt(List<Message> messageList) {
        ChatGptRequest request = new ChatGptRequest("gpt-3.5-turbo", messageList);
        return gptClient(request);
    }

    public LLMResponseDto structuredWithGpt(List<Message> messageList) throws JsonProcessingException {
        Map<String, Object> functionSchema = jpabasic.truthaiserver.dto.prompt.adapter.openAIAdapter.functionSchema();
        System.out.println("🤨functionSchema:" + functionSchema.entrySet());
        ChatGptRequest request = new ChatGptRequest("gpt-3.5-turbo", messageList, List.of(functionSchema), Map.of("name", "get_structured_answer"));
        System.out.println("🤨request:" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
        return gptClientStructured(request);
    }

    public String gptClient(ChatGptRequest request) {
        //webClient로 OpenAI로 호출
        ChatGptResponse chatGptResponse = openAiWebClient.post()
                .uri(gptUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGptResponse.class)
                .block();
        return chatGptResponse.getChoices().get(0).getMessage().getContent();
    }

    public LLMResponseDto gptClientStructured(ChatGptRequest request) throws JsonProcessingException {
        //webClient로 OpenAI로 호출
        ChatGptResponse chatGptResponse = openAiWebClient.post()
                .uri(gptUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGptResponse.class)
                .block();
        String argumentsJson = chatGptResponse
                .getChoices()
                .get(0)
                .getMessage()
                .getFunction_call()
                .getArguments(); // 💡 이 부분이 JSON 문자열!

        System.out.println("⭐ argumentsJson:" + argumentsJson);

        return objectMapper.readValue(argumentsJson, LLMResponseDto.class);
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
}
