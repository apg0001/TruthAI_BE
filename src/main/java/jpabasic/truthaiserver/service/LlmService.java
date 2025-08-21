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
    private final ClaudeAdapter claudeAdapter;


    public LlmService(WebClient.Builder webClientBuilder, WebClient openAiWebClient, WebClient claudeClient, WebClient geminiClient, WebClient perplexityClient, ClaudeAdapter claudeAdapter, openAIAdapter openAIAdapter) {
        this.webClientBuilder = webClientBuilder;
        this.openAiWebClient = openAiWebClient;
        this.claudeClient = claudeClient;
        this.geminiClient = geminiClient;
        this.perplexityClient = perplexityClient;
        this.claudeAdapter = claudeAdapter;
        this.openAIAdapter = openAIAdapter;
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);


    public String createGptAnswer(String question) {
        ChatGptRequest request = new ChatGptRequest("gpt-3.5-turbo", question);
        return gptClient(request);
    }

    public String createClaudeAnswer(String question) {
        ClaudeRequestDto request = new ClaudeRequestDto(question);
        return claudeClient(request);
    }

    public String createGeminiAnswer(String question) {
        GeminiRequestDto request = GeminiRequestDto.send(question);
        System.out.println("🏃GeminiRequestDto:" + request);
        return geminiClient(request);
    }

    public String createPerplexityAnswer(String question){
        PerplexityRequestDto request=new PerplexityRequestDto(question);
        return perplexityClient(request);
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

    public LLMResponseDto structuredWithClaude(
            List<jpabasic.truthaiserver.dto.answer.Message> messageList
    ) throws JsonProcessingException {

        // 1) system 모으기 (top-level 문자열)
        StringBuilder systemBuf = new StringBuilder();

        // 2) user/assistant를 Anthropic 메시지로 변환 (원래 순서 유지)
        List<ClaudeRequestDto.Message> msgs = new ArrayList<>();

        for (var msg : messageList) {
            String role = normalizeRole(msg.getRole());        // ↓ 아래 헬퍼
            String text = msg.getContent() == null ? "" : msg.getContent();

            if ("system".equals(role)) {
                if (systemBuf.length() > 0) systemBuf.append("\n\n");
                systemBuf.append(text);
                continue;
            }

            // content는 항상 blocks 배열이어야 함
            var block = ClaudeRequestDto.Content.text(text);
            msgs.add(new ClaudeRequestDto.Message(role, List.of(block)));
        }

        // 2-1) 최소 형식 검증 (초기 호출은 user로 시작/끝 권장)
        if (msgs.isEmpty() || !"user".equals(msgs.get(0).getRole())) {
            throw new IllegalArgumentException("Anthropic messages must start with a 'user' message.");
        }
        if (!"user".equals(msgs.get(msgs.size() - 1).getRole())) {
            // 마지막이 assistant면 가끔 거절됨 → 짧은 user 프롬프트를 덧붙여 마무리
            msgs.add(ClaudeRequestDto.Message.userText("Please continue."));
        }

        // 3) tools 구성 (구조화 정답 툴)
        var tools = List.of(ClaudeAdapter.createStructuredAnswerTool());

        // 4) 요청 생성
        ClaudeRequestDto req = new ClaudeRequestDto(
                "claude-3-5-sonnet-20241022",
                systemBuf.length() == 0 ? null : systemBuf.toString(),
                tools,
                msgs
        );
        req.setMaxTokens(1024);
        // 구조화 강제하고 싶으면:
        // req.setToolChoice(ClaudeRequestDto.toolChoiceForce("get_structured_answer"));
        // 자동 위임하려면:
        // req.setToolChoice(ClaudeRequestDto.toolChoiceAny());

        if (log.isDebugEnabled()) {
            log.debug("Claude request:\n{}",
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(req));
        }

        System.out.println("🍪 요청 생성 완료 :LLMService, structuredWithClaude");

        // 5) 호출 + 파싱
        return claudeClientStructured(req);
    }

    private String normalizeRole(String r) {
        if (r == null) return "user";
        r = r.toLowerCase();
        if (r.equals("user") || r.equals("assistant") || r.equals("system")) return r;
        // 알 수 없는 역할은 user로 취급 (API가 user/assistant만 허용)
        return "user";
    }


    public String createGeminiAnswerWithPrompt(GeminiRequestDto request) {
        return geminiClient(request);
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


    public LLMResponseDto claudeClientStructured(ClaudeRequestDto request) {
        // 0) 요청 JSON 로그로 먼저 확인 (messages[].content가 blocks 배열인지 꼭 체크)
        if (log.isDebugEnabled()) {
            try {
                log.debug("Claude request: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
            } catch (Exception ignore) {
            }
        }

        System.out.println("🍪 request : " + request);

        // 1) 호출 (에러 바디를 그대로 받아서 예외에 붙임)
        ClaudeResponseDto resp = claudeClient.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchangeToMono(res -> {
                    int code = res.statusCode().value();
                    if (res.statusCode().isError()) {
                        return res.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> {
                                    String ra = res.headers().asHttpHeaders().getFirst("Retry-After");
                                    Long raSec = parseRetryAfterSeconds(ra); // 숫자/HTTP-date → 초
                                    if (code == 529 || (code >= 500 && code < 600)) {
                                        return Mono.error(new BusinessException(CLAUDE_HTTP_ERROR));
                                    }
                                    return Mono.error(new BusinessException(CLAUDE_HTTP_ERROR));
                                });
                    }
                    return res.bodyToMono(ClaudeResponseDto.class);
                })
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(1))
                                .maxBackoff(Duration.ofSeconds(20))
                                .jitter(0.3)

                )
                .block();


        System.out.println("🍪 ClaudeResponse:" + resp);
        if (resp == null) throw new BusinessException(CLAUDE_ANSWER_EMPTY1);

        List<ClaudeResponseDto.Content> blocks = resp.getContent();
        if (blocks == null || blocks.isEmpty()) {
            // 응답 자체는 왔지만 content가 빈 경우: 요청 스키마/모델명/헤더 문제일 확률 높음
            throw new BusinessException(CLAUDE_ANSWER_EMPTY2);
        }

        // 2) tool_use(get_structured_answer) 우선 처리
        for (ClaudeResponseDto.Content c : blocks) {
            if ("tool_use".equalsIgnoreCase(c.getType()) && c.getToolUse() != null) {
                var tu = c.getToolUse();
                if ("get_structured_answer".equals(tu.getName())) {
                    return mapStructuredAnswer(tu.getInput()); // {answer, sources[]} → DTO
                }
            }
        }

        // 3) fallback: text 블록을 합쳐 JSON 시도 → 실패 시 평문
        String raw = blocks.stream()
                .filter(b -> "text".equalsIgnoreCase(b.getType()))
                .map(ClaudeResponseDto.Content::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining())
                .trim();

        if (raw.isEmpty()) throw new BusinessException(CLAUDE_ANSWER_EMPTY3);

        try {
            Map<String, Object> m = objectMapper.readValue(raw, new TypeReference<>() {
            });
            return mapStructuredAnswer(m);
        } catch (Exception ignore) {
            return new LLMResponseDto(raw, List.of());
        }
    }


    @SuppressWarnings("unchecked")
    private LLMResponseDto mapStructuredAnswer(Map<String, Object> m) {
        String answer = String.valueOf(m.getOrDefault("answer", ""));
        List<Map<String, String>> srcs =
                (List<Map<String, String>>) m.getOrDefault("sources", List.of());

        List<LLMResponseDto.SourceResponseDto> sources = new ArrayList<>();
        for (Map<String, String> s : srcs) {
            sources.add(new LLMResponseDto.SourceResponseDto(
                    s.getOrDefault("title", ""),
                    s.getOrDefault("url", "")
            ));
        }
        return new LLMResponseDto(answer, sources);
    }

    private Long parseRetryAfterSeconds(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            // 숫자(초) 형식 우선
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            // HTTP-date 형식 지원 (간단 파서)
            try {
                var formatter = java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
                var when = java.time.ZonedDateTime.parse(v, formatter);
                long sec = java.time.Duration.between(java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC), when).getSeconds();
                return Math.max(sec, 0);
            } catch (Exception ignore) {
                return null;
            }
        }
    }


    public String claudeClient(ClaudeRequestDto request) {

//        ClaudeRequestDto request=new ClaudeRequestdto("claude-3-5-sonnet-20241022",question);


        //WebClient로 ClaudeAI로 호출
        ClaudeResponseDto claudeResponseDto = claudeClient.post()
                .uri("/v1/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponseDto.class)
                .block();
        return claudeResponseDto.getContent().get(0).getText();

    }


    public String geminiClient(GeminiRequestDto request) {

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

        return response
                .getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getText();

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



