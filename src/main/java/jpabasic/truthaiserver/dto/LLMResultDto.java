package jpabasic.truthaiserver.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LLMResultDto {
    private String model;
    private String opinion;
    private double score;
}
