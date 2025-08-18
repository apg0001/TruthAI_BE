package jpabasic.truthaiserver.dto.answer.gemini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeminiRequestDto {

    private SystemInstruction systemInstruction; //시스템 프롬프트
    private List<Contents> contents; //대화 기록
    private GenerationConfig generationConfig;

    public static GeminiRequestDto fromText(String systemPrompt,String text) {
        return new GeminiRequestDto(
                new SystemInstruction(List.of(new Part(systemPrompt))),
                List.of(new Contents(List.of(new Part(text)))),
                new GenerationConfig(1024,0.0,null)
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
