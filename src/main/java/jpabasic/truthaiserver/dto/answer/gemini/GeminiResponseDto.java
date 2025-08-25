package jpabasic.truthaiserver.dto.answer.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
public class GeminiResponseDto {
    private List<Candidate> candidates;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Candidate {
        private Content content;

        @JsonProperty("groundingMetadata")
        private GroundingMetadata groundingMetadata; // <-- 여기에 존재
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Part {
        private String text;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GroundingMetadata {
        @JsonProperty("groundingChunks")
        private List<GroundingChunk> groundingChunks;

        @JsonProperty("searchEntryPoint")
        private SearchEntryPoint searchEntryPoint; // (선택) 검색 서제스트 UI
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GroundingChunk {
        private Web web; // {"web":{"uri":..., "title":...}}
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Web {
        private String uri;
        private String title;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class SearchEntryPoint {
        private String renderedContent; // HTML 스니펫
    }
}



