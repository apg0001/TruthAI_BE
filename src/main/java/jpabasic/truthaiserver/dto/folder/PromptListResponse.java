package jpabasic.truthaiserver.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PromptListResponse {
    private Long id;
    private String summary;
    private LocalDateTime createdAt;
}
