package jpabasic.truthaiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LLMResultDto {
    private String model; // GPT, GEMINI, CLAUDE
    private String opinion; // 환각 의심도: 좋음, 보통, 나쁨
    private double score; // 유사도
//    private List<String> source;
}
