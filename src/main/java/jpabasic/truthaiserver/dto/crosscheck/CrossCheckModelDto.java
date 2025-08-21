package jpabasic.truthaiserver.dto.crosscheck;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CrossCheckModelDto {
    private Integer hallucinationLevel; // 0~N
    private Integer similarity;        // percent 0~100
    private List<CrossCheckReferenceDto> references; // nullable/empty
}


