package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.claude.ClaudeRequestDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ClaudeAdapter extends BasePromptTemplate{

    //Claude 용으로 재구성
    public ClaudeRequestDto toClaudeRequest(
            Message message,
            String persona,
            PromptDomain domain
    ){

        List<Message> msgs=super.render(message,persona,domain);


        // system 파트들을 블록별 문자열로 수집
        List<String> systemBlocks=new ArrayList<>();
        systemBlocks.add(systemIdentity(domain,persona));

        //1. System 문자열 구성 : systemIdentity + global + domain (+few-shot)
        String global=globalGuidelines();
        if(global!=null) systemBlocks.add(global);

        //domainGuideLines는 선택사항
        String domainExtra = domainGuidelines();
        if (!domainExtra.isBlank()) {
            systemBlocks.add(domainExtra);
        }

        //few-shot도 선택사항
        String fewshot = fewShotExamples(new HashMap<>());
        if (!fewshot.isBlank()) {
            systemBlocks.add(fewshot);
        }

        List<ClaudeRequestDto.ClaudeTextBlock> sys=ClaudeRequestDto.ClaudeTextBlock.ofTexts(systemBlocks);

        return new ClaudeRequestDto(sys,message.getContent());
    }
}
