package jpabasic.truthaiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CrossCheckListDto {
    private Long answerId;
    private Long promptId;
    private String model;        // GPT / CLAUDE / ...
    private String opinion;      // 환각 가능성 등
    private Float score;         // Answer에 저장된 최종 점수
}