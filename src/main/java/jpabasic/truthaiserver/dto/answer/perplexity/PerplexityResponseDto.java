package jpabasic.truthaiserver.dto.answer.perplexity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jpabasic.truthaiserver.domain.Source;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.LLMResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true) //불필요한 필드 무시 설정
public class PerplexityResponseDto {

    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("search_results")
    private List<SearchResults> searchResults;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private Message message;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchResults {
        private String title;
        private String url;
    }




}
