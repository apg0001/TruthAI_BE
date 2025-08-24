package jpabasic.truthaiserver.dto.crosscheck;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrossCheckListDto {
    private Long answerId;
    private Long promptId;
    private String model;        // GPT / CLAUDE / ...
    private String content;
    private Integer level;       // 환각 레벨 (0: 낮음, 1: 중간, 2: 높음)
    private Float score;         // Answer에 저장된 최종 점수
}