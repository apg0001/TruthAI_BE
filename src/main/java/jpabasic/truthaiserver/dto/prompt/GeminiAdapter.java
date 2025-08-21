package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiAdapter extends BasePromptTemplate{

    //Gemini 용으로 재구성
    public GeminiRequestDto toGeminiRequest(
            Message message,
            String persona,
            PromptDomain domain
    ) {

        List<Message> msgs=super.render(message,persona,domain);

        // system 파트들을 블록별 문자열로 수집
        List<String> systemBlocks=new ArrayList<>();
        systemBlocks.add(systemIdentity(domain,persona));//이건 도대체 어떻게 되는거지 샤갈

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

        GeminiRequestDto.SystemInstruction sys= GeminiRequestDto.SystemInstruction.ofTexts(systemBlocks);

        //2. message 구성
        List<GeminiRequestDto.Contents> contents=msgs.stream()
                .filter(m->!"system".equalsIgnoreCase(m.getRole()))
                .map(m->{
//                    String role="user".equalsIgnoreCase(m.getRole())?"user":"model";
                    return new GeminiRequestDto.Contents(
                            "user",
                            List.of(new GeminiRequestDto.Part(m.getContent())
                    ));
                })
                .toList();

        GeminiRequestDto.GenerationConfig gen=
                new GeminiRequestDto.GenerationConfig(1024,0.0,null);

        return new GeminiRequestDto(sys,contents,gen);
    }



}
