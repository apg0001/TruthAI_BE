package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequest;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeResponse;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiResponseDto;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
import jpabasic.truthaiserver.dto.answer.openai.ChatGptResponse;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.repository.AnswerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static jpabasic.truthaiserver.domain.LLMModel.CLAUDE;
import static jpabasic.truthaiserver.domain.LLMModel.GPT;
import static jpabasic.truthaiserver.domain.LLMModel.GEMINI;
import static jpabasic.truthaiserver.domain.LLMModel.PERPLEXITY;

@Service
@Slf4j
public class AnswerService {

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
    private final AnswerRepository answerRepository;

    public AnswerService(WebClient.Builder webClientBuilder,WebClient openAiWebClient,WebClient claudeClient,WebClient geminiClient,AnswerRepository answerRepository) {
        this.webClientBuilder = webClientBuilder;
        this.openAiWebClient = openAiWebClient;
        this.claudeClient=claudeClient;
        this.geminiClient=geminiClient;
        this.answerRepository=answerRepository;
    }


    public List<LlmAnswerDto> getLlmAnswers(List<LLMModel> models,String question) {

        return selectAnswer(models, question);
    }


    public List<LlmAnswerDto> selectAnswer(List<LLMModel> models, String question) {

        if (models == null || models.isEmpty()) {
            throw new BusinessException(ErrorMessages.LLM_NULL_ERROR);
        }

        return models.stream()
                .map(model -> switch (model) {
                    case GPT -> saveLlmAnswer(GPT, createGptAnswer(question));
                    case CLAUDE -> saveLlmAnswer(CLAUDE, createClaudeAnswer(question));
                    //            case PERPLEXITY -> new LlmAnswerDto(PERPLEXITY, createAnswer(PERPLEXITY));
                    case GEMINI -> saveLlmAnswer(GEMINI, createGeminiAnswer(question));
                    default -> throw new BusinessException(ErrorMessages.LLM_MODEL_ERROR);
                })
                .toList();
    }


    public LlmAnswerDto saveLlmAnswer(LLMModel model,String answer){

        LlmAnswerDto answerDto = new LlmAnswerDto(model,answer);
        Answer answerEntity=answerDto.toEntity();
        answerRepository.save(answerEntity);
        return answerDto;

    }


    public String createGptAnswer(String question) {

        ChatGptRequest request=new ChatGptRequest("gpt-3.5-turbo",question);

        //webClient로 OpenAI로 호출
        ChatGptResponse chatGptResponse=openAiWebClient.post()
                .uri(gptUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatGptResponse.class)
                .block();
        return chatGptResponse.getChoices().get(0).getMessage().getContent();

    }


    public String createClaudeAnswer(String question){

        ClaudeRequest request=new ClaudeRequest("claude-3-5-sonnet-20241022",question);

        //WebClient로 ClaudeAI로 호출
        ClaudeResponse claudeResponse=claudeClient.post()
                .uri("")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .block();
        return claudeResponse.getContent().get(0).getText();

    }


    public String createGeminiAnswer(String question){

        GeminiRequestDto request = GeminiRequestDto.fromText(question);

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
