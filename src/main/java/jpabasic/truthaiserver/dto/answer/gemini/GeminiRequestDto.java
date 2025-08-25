package jpabasic.truthaiserver.dto.answer.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class GeminiRequestDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SystemInstruction systemInstruction; //시스템 프롬프트

    private List<Contents> contents; //대화 기록

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Tools> tools;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private GenerationConfig generationConfig;

    public GeminiRequestDto(SystemInstruction sys, List<Contents> contents, GenerationConfig gen) {
        this.systemInstruction = sys;
        this.contents = contents;
    }

    public static GeminiRequestDto toGoogleSearch(GeminiRequestDto dto) {
        return new GeminiRequestDto(
                dto.getSystemInstruction(),    // ✅ 그대로 전달
                dto.getContents(),
                List.of(Tools.googleSearch()), // ✅ 최신 google_search 추가
                dto.getGenerationConfig()
        );
    }


    //gemini grounding
    public static GeminiRequestDto fromText(String systemPrompt,String text) {
        return new GeminiRequestDto(
                new SystemInstruction(List.of(new Part(systemPrompt))),
                List.of(new Contents(List.of(new Part(text)))),
                //tools : 신형 google_search 사용
                List.of(Tools.googleSearch()),
                new GenerationConfig(1024,0.0,null)
        );
    }

    public static GeminiRequestDto send(String text) {

        return new GeminiRequestDto(
                null,
                List.of(new Contents(List.of(new Part(text)))),
                null,
                new GenerationConfig(1024, 0.0, null)
        );
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contents {
        private String role; //user, model
        private List<Part> parts;

        public Contents(List<Part> parts) {
            role="user";
            this.parts=parts;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Tools {
        // 신형(권장): Gemini 2.x/2.5 및 최신 1.5에서도 지원
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Map<String,Object> google_search;

        // 레거시(1.5 전용 옵션): 동적 검색 설정이 필요할 때만
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private GoogleSearchRetrieval google_search_retrieval;

        @Data @NoArgsConstructor @AllArgsConstructor
        public static class GoogleSearchRetrieval {
            @JsonInclude(JsonInclude.Include.NON_NULL)
            private DynamicRetrievalConfig dynamic_retrieval_config;

            @Data @NoArgsConstructor @AllArgsConstructor
            public static class DynamicRetrievalConfig {
                // MODE_DYNAMIC, MODE_DISABLED 등 (문자열로 보냄)
                private String mode;           // e.g., "MODE_DYNAMIC"
                private Double dynamic_threshold; // 0.0~1.0 (예: 0.7)
            }
        }

        public static Tools googleSearch() {
            return new Tools(Collections.emptyMap(), null);
        }

        public static Tools googleSearchRetrieval(String mode, Double threshold) {
            return new Tools(null,
                    new GoogleSearchRetrieval(new GoogleSearchRetrieval.DynamicRetrievalConfig(mode, threshold)));
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemInstruction {
        private List<Part> parts;

        //한 덩어리 text
        public SystemInstruction(String text) {
            this.parts = new ArrayList<>();
            this.parts.add(new Part(text));
        }

        public static SystemInstruction ofTexts(String...texts){
            List<Part> ps=new ArrayList<>();
            for(String t: texts){
                if (t!=null && !t.isBlank()) ps.add(new Part(t));
            }
            return new SystemInstruction(ps);
        }
        public static SystemInstruction ofTexts(List<String> texts){
            return ofTexts(texts.toArray(new String[0]));
        }
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class GenerationConfig {
        private Integer maxOutputTokens;
        private Double temperature;
        private List<String> stopSequences;
    }
}
