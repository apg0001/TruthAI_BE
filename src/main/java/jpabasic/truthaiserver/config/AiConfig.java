package jpabasic.truthaiserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;



@Configuration
@Slf4j
public class AiConfig {

    @Value("${openai.api.key}")
    private String gptApiKey;

    @Value("${openai.api.url}")
    private String gptApiUrl;

    @Value("${claude.api.url}")
    private String claudeApiUrl;

    @Value("${claude.api.key}")
    private String claudeApiKey;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${perplexity.api.url}")
    private String perplexityApiUrl;

    @Value("${perplexity.api.key}")
    private String perplexityApiKey;

    private final WebClient.Builder webClientBuilder;

    public AiConfig(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Bean
    public WebClient openAiWebClient() {
        return webClientBuilder
                .baseUrl(gptApiUrl)
                .defaultHeader("Authorization","Bearer "+gptApiKey)
                .build();
    }

    @Bean
    public WebClient claudeClient(){
        log.info("✅ url:{}",claudeApiUrl);
        log.info("✅ apiKey:{}",claudeApiKey);

        return webClientBuilder
                .baseUrl(claudeApiUrl)
                .defaultHeader("x-api-key",claudeApiKey)
                .defaultHeader("anthropic-version","2023-06-01")
                .build();
    }

    @Bean
    public WebClient geminiClient(){
        return webClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta")
                .build();
    }

    @Bean
    public WebClient perplexityClient(){
        return webClientBuilder
                .baseUrl(perplexityApiUrl)
                .defaultHeader("Authorization","Bearer "+perplexityApiKey)
                .build();
    }


}
