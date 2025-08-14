package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.dto.answer.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePromptTemplate implements PromptTemplate {

    @Override 
    //프롬프트 최적화 
    public List<Message> render(Message message, String persona,String domain) {
        List<Message> msgs = new ArrayList<>();
        
        msgs.add(new Message("system", systemIdentity(domain,persona)));    
        return executeInternalRender(message, msgs);
    }

    @Override
    // 내용 요약
    public List<Message> render(Message message) {
        List<Message> msgs = new ArrayList<>();
        return executeInternalRender(message,msgs);
    }


    private List<Message> executeInternalRender(Message message, List<Message> msgs) {
        Map<String, Object> vars=new HashMap<>();

        msgs.add(new Message("system", globalGuidelines()));     // 전역 가드레일
        String domain = domainGuidelines();
        if (domain != null && !domain.isBlank()) {
            msgs.add(new Message("system", domain));             // 도메인별 지침
        }
        String fewshot = fewShotExamples(vars);
        if (fewshot != null && !fewshot.isBlank()) {
            msgs.add(new Message("system", fewshot));            // 예시(Few-shot)
        }
        msgs.add(new Message("user", userContent(message)));        // 사용자 입력(동적)

        return msgs;
    }

    // === 아래 훅(Hook) 메서드들만 서브클래스에서 바꿔 끼움 ===
    protected abstract String systemIdentity(String domain,String persona);

    protected String globalGuidelines() {
        return
                """
                Take a deep breath and let's work this out.\s
                Be sure we have the right answer.
                """;
    }

    // 도메인별 추가 규칙 (없으면 빈 문자열 반환)
    protected abstract String domainGuidelines();


    // Few-shot 예시(필요 시)
    protected String fewShotExamples(Map<String, Object> vars) { return ""; }


    // 사용자 입력을 최종적으로 문자열로 구성
    protected abstract String userContent(Message message);
}
