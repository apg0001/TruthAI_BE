package jpabasic.truthaiserver.dto.prompt.sidebar;


import jpabasic.truthaiserver.domain.Prompt;

public record SideBarPromptListDto (Long promptId, String summary){

    public static SideBarPromptListDto toDto(Prompt prompt)
        {
            return new SideBarPromptListDto(prompt.getId(), prompt.getSummary());
        }
}
