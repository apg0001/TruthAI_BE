package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequestDto;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeResponse;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiResponseDto;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptResponse;
import jpabasic.truthaiserver.dto.prompt.PromptAnswerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LlmService {

    @Value("${openai.api.url}")
    private String gptUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${claude.api.url}")
    private String claudeApiUrl;

    private final WebClient.Builder webClientBuilder;
    private final WebClient openAiWebClient;
    private final WebClient claudeClient;
    private final WebClient geminiClient;
    private final WebClient perplexityClient;

    public LlmService(WebClient.Builder webClientBuilder,WebClient openAiWebClient,WebClient claudeClient,WebClient geminiClient,WebClient perplexityClient) {
        this.webClientBuilder = webClientBuilder;
        this.openAiWebClient = openAiWebClient;
        this.claudeClient=claudeClient;
        this.geminiClient=geminiClient;
        this.perplexityClient=perplexityClient;

    }

    public String createGptAnswer(String question) {
        ChatGptRequest request=new ChatGptRequest("gpt-3.5-turbo",question);
        return gptClient(request);
    }

    public String createClaudeAnswer(String question) {
        ClaudeRequestDto request=new ClaudeRequestDto(question);
        return claudeClient(request);
    }

    public String createGeminiAnswer(String question) {
        GeminiRequestDto request = GeminiRequestDto.fromText(null,question);
        return geminiClient(request);
    }

    public String createGptAnswerWithPrompt(List<Message> messageList){
        ChatGptRequest request=new ChatGptRequest("gpt-3.5-turbo",messageList);
        return gptClient(request);
    }

    public String createClaudeAnswerWithPrompt(ClaudeRequestDto request){
//        ClaudeRequestDto request=new ClaudeRequestDto(messageList);
        return claudeClient(request);
    }

    public String createGeminiAnswerWithPrompt(GeminiRequestDto request){
        return geminiClient(request);
    }

    //LLM 응답을 dto로 반환
//    public PromptAnswerDto seperateAnswers(Long promptId,String response){
//        return new PromptAnswerDto(promptId,response);
//    }

    public List<Map<LLMModel,PromptAnswerDto>> seperateAnswers(Long promptId, List<Map<LLMModel,String>> answers){
        return answers.stream()
                .flatMap(m->m.entrySet().stream())
                .map(e->Map.of(e.getKey(),new PromptAnswerDto(promptId,e.getValue())))
                .toList();
    }

    public String gptClient(ChatGptRequest request){
        //webClient로 OpenAI로 호출
        ChatGptResponse chatGptResponse=openAiWebClient.post()
                .uri(gptUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGptResponse.class)
                .block();
        return chatGptResponse.getChoices().get(0).getMessage().getContent();
    }


    public String claudeClient(ClaudeRequestDto request){

//        ClaudeRequest request=new ClaudeRequest("claude-3-5-sonnet-20241022",question);

        //WebClient로 ClaudeAI로 호출
        ClaudeResponse claudeResponse=claudeClient.post()
                .uri("/v1/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .block();
        return claudeResponse.getContent().get(0).getText();

    }


    public String geminiClient(GeminiRequestDto request){

//        GeminiRequestDto request = GeminiRequestDto.fromText(question);

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

//    public String createPerplexityAnswer(String question){
//
//        PerplexityResponseDto response=perplexityClient.post()
//                .uri("")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(PerplexityResponseDto.class)
//                .block();
//        return response.getChoices(0).getMessage().getContent();
//    }
}
