package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("editable")
public class EditablePromptTemplate extends BasePromptTemplate {

    @Override
    public String key(){
        return "editable";
    }

    @Override
    public String systemIdentity(PromptDomain domain, String persona){
        return String.format("당신은 %s 전문가이고 %s 대상으로 해당 내용을 알려주어야 합니다.",domain,persona);
    }

    @Override
    protected String globalGuidelines(){
        return "";
    }


    @Override
    protected String fewShotExamples(PromptDomain domain, String persona, Map<String,Object> vars) {
        String userQuestion = String.valueOf(vars.getOrDefault("userQuestion", "주제에 대해 알려줘"));
        String personaPrompt=systemIdentity(domain,persona);
        String topic=domain.toString();

        return """
            다음 예시를 참고해서, 최적화된 프롬프트를 만들어 주세요.

            - 예시 1 :
            유저의 질문 : %s
            답변 : %s. 아래 조건에 맞춰 답변을 정리해 주세요.
            1) '핵심 개념', '응용·사례', '리스크·이슈' 세 가지 카테고리로 구분할 것
            2) 각 카테고리별 최소 3가지 핵심 포인트를 bullet point로 제시할 것
            3) 각 포인트는 명확하고 간결한 한두 문장으로 설명할 것
            4) 가능하면 간단한 예시나 실제 사례를 함께 제시할 것

            - 예시 2 :
            유저의 질문 : %s
            답변 : %s. 아래 조건에 맞춰 답변을 정리해 주세요.
            1) '장점'과 '단점' 두 카테고리로 구분할 것
            2) 각 카테고리별 최소 3가지 핵심 포인트를 bullet point로 제시할 것
            3) 각 포인트는 명확하고 간결한 한두 문장으로 설명할 것
            4) 가능하면 간단한 예시(상황/사례)를 함께 제시할 것
            """.formatted(
                userQuestion, personaPrompt,
                userQuestion, personaPrompt
        );
    }


    @Override
    protected String userContent(Message message){
        String text=message.getContent();
        return """
               
                사용자가 ``` %s ```라고 물었습니다. \s
                답변은 다음 조건을 충족하세요: \s
                
                1. **구조화**: "장점"과 "단점" 두 가지 큰 카테고리로 나누어 작성. \s
                2. **항목 수**: 각 카테고리별 최소 3가지 핵심 포인트를 bullet point 형식으로 제시. \s
                3. **설명 방식**: \s
                   - 명확하고 간결한 문장으로 설명. \s
                   - 가능하다면 간단한 예시를 포함. \s
                4. **톤**: 전문적이지만 이해하기 쉽게. \s
                
                이 조건에 맞춰 답변을 작성하세요.
                
                
                """.formatted(text);
    }



}
