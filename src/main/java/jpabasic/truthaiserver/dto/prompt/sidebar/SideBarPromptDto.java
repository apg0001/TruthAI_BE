package jpabasic.truthaiserver.dto.prompt.sidebar;

import jpabasic.truthaiserver.domain.Answer;

public record SideBarPromptDto (
        Long promptId, String originalPrompt, String optimizedPrompt,
        String summary, Answer prompt
        ){
}
