package jpabasic.truthaiserver.dto.prompt.template;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("editable")
public class EditablePromptTemplate extends BasePromptTemplate {

    @Override
    public String key() {
        return "editable";
    }

    @Override
    public String systemIdentity(PromptDomain domain, String persona) {
        return
                """
                        당신은 프롬프트 엔지니어입니다.
                        목표: 사용자의 원질문을 바탕으로, 다른 AI가 정확하고 상세히 답하도록 하는 **최적화된 사용자 프롬프트**만 작성합니다.
                        절대 질문에 대한 직접적인 답변을 작성하지 않습니다.
                        
                        """;

    }

    @Override
    protected String globalGuidelines() {
        return

                """
                        [행동 규칙]
                        - 절대 질문에 대한 '답변'을 작성하지 말 것. 오직 '사용자 프롬프트'만 작성.
                        - 출력은 마크다운 코드블록 하나만. 코드블록 라벨은 prompt.
                        - 불필요한 서론/결론/자기언급 금지.
                        
                         [출력 형식]
                        ```prompt
                        (여기에 최종 사용자 프롬프트만)
                        
                        """;
    }


    @Override
    protected String fewShotExamples(Map<String, Object> vars) {
        String userQuestion = String.valueOf(vars.getOrDefault("userQuestion", "주제에 대해 알려줘"));

        return """
            [FEW-SHOT EXAMPLES]
            
            <예시 1>
            유저의 질문 : ai에 대해서 알려줘
            어시스턴트 :
            ```prompt
            [역할] 당신은 인공지능 전문가입니다.
            [질문] "ai에 대해서 알려줘"
            [목표] 최신 동향을 '기술·산업·정책' 3가지 관점에서 간결하고 체계적으로 설명하도록 유도한다.
            [출력 구조]
          
            [작성 규칙]
            - 각 bullet은 1–2문장. 가능하면 간단한 예시/사례 포함.
            - 사실 기반 우선, 불확실하면 불확실하다고 명시.
            - 질문과 동일한 언어로 작성.
            [금지]
            - 메타 텍스트/서론/결론 출력 금지. 위 구조만 출력.
            <예시 2 — 장단점 구조>
            유저의 질문 : 탄수화물이 장단점에 대해서 알려줘
            어시스턴트 :
            
            
            <실제 사용자 입력>
            유저의 질문 : %s
            """.formatted(userQuestion);

    }


    @Override
    protected String userContent(Message message) {
        String text = message.getContent();
        return """
                
                사용자의 원질문: "%s"
                
                당신의 임무는 위 질문에 대해 다른 AI에게 전달할 **최적화된 사용자 프롬프트**를 작성하는 것입니다.
                ※ 정답을 쓰지 말고, **프롬프트 문구만** 작성하세요.
                
                [프롬프트에 반드시 포함할 요소]
                - 목표: 질문에 대해 정확하고 상세한 설명을 이끌어내는 목표 명시
                - 출력 구조:
                  - 질문에 '장단점/장점/단점/pros/cons'가 포함되면 → "장점 / 단점" 두 섹션, 각 최소 3개 bullet
                  - 그 외의 경우 → "핵심 개념 / 응용·사례 / 리스크·이슈" 세 섹션, 각 최소 3개 bullet
                - 작성 규칙: 각 bullet은 1–2문장, 가능한 경우 간단한 예시·수치·반례 포함
                - 정확성: 추정은 분리해 표기, 모르면 모른다고 말하고 대안/다음 단계 제시
                - 언어: 질문과 동일한 언어로 답하도록 지시
                - 금지: 메타설명/사족/자기언급 금지(프롬프트 외 텍스트 출력 금지)
                
                [출력 형식]
                - 아래 형식의 마크다운 코드블록 **하나만** 출력:
                ```prompt
                (여기에 최종 사용자 프롬프트만)
                
                
                """.formatted(text);
    }


}
