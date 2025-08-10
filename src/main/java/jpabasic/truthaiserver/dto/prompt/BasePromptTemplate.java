package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.dto.answer.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePromptTemplate implements PromptTemplate {

    @Override
    public List<Message> render(Message message) {
        List<Message> msgs = new ArrayList<>();
        Map<String, Object> vars=new HashMap<>();

        msgs.add(new Message("system", systemIdentity()));       // 역할/톤
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
    protected String systemIdentity() {
        return "너는 한국어로 답하는 믿을 수 있는 전문가 비서야. 불확실하면 모른다고 말해.";
    }

    protected String globalGuidelines() {
        return """
            # 전역 가드레일
            - 사실과 의견을 구분해.
            - 과도한 추측 금지, 출처 불명 확정적 표현 금지.
            - 목록이 필요하면 간결하게.
            """;
    }

    // 도메인별 추가 규칙 (없으면 빈 문자열 반환)
    protected String domainGuidelines() { return ""; }

    // Few-shot 예시(필요 시)
    protected String fewShotExamples(Map<String, Object> vars) { return ""; }

    // 사용자 입력을 최종적으로 문자열로 구성
    protected abstract String userContent(Message message);
}
