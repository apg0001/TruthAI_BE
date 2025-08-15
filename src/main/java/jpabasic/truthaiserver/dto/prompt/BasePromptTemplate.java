package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePromptTemplate implements PromptTemplate {

    @Override
    public List<Message> render(Message message, String persona, PromptDomain domain) {
        List<Message> msgs = new ArrayList<>();

        msgs.add(new Message("system", systemIdentity(domain, persona)));
        return executeInternalRender(message, msgs);
    }

    @Override
    // 내용 요약
    public List<Message> render(Message message) {
        List<Message> msgs = new ArrayList<>();
        return executeInternalRender(message, msgs);
    }


    private List<Message> executeInternalRender(Message message, List<Message> msgs) {
        Map<String, Object> vars = new HashMap<>();

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
    protected abstract String systemIdentity(PromptDomain domain, String persona);

    protected String globalGuidelines() {
        return
                """
                                ⭐ You are a fact-verification-based answering system.
                                ⭐ Never generate any information unless the source is 100% clearly verified.
                                ⭐ Do not provide guesses, hypothetical scenarios, analogies, or examples.
                                ⭐ If no source is available, you must respond with "No source available" only.
                        """;
    }

    // 도메인별 추가 규칙 (없으면 빈 문자열 반환)
    protected String domainGuidelines() {
        return "";
    }


    // Few-shot 예시(필요 시)
    protected String fewShotExamples(Map<String, Object> vars) {
        return
                """
                        Always follow these forms.
                        🚨 But remember if there's no sources, just say there's no sources.
                        🚨 Do not ever guess yourself.
                     
                        
                        ## Answer
                                - give the conclusion of question, and tell the simple reason/context.
                                - Mark controversial/uncertain parts.
                        ## Why
                                - Suggest a core reason of your answer in one sentence.
                        ## Sources
                                - suggest more than 2 trustful sources with url.(official report,thesis,newspaper,legacy media)
                                - (ex.)
                                    - source 1: url
                                    - source 2: url
                        
                        these are few examples.
                                ## Answer
                                    비타민 C가 감기를 완전히 예방한다는 주장은 과장입니다. 일부 연구에서 증상 기간 단축이나 경미한 완화가 보고되었지만,
                                    일관된 예방 효과는 확인되지 않았습니다. 감기 예방에는 손 위생, 충분한 수면, 예방접종(인플루엔자) 등 종합적 관리가 중요합니다.
                        
                                    ## Why
                                    무작위대조시험 종합 결과가 예방 효과의 일관성을 뒷받침하지 않습니다.
                        
                                    ## Sources
                                    - Source 1: Hemilä H, Chalker E. Cochrane Review (2013, updated).\s
                                    - Source 2: CDC. Common Cold: Prevention & Treatment.
                        
                                ---------------------------
                        
                                 ## Answer
                                    블록체인 거래가 모두 완전한 익명이라는 주장은 부정확합니다. 비트코인은 가명성(pseudonymity)에 가깝고,
                                    온체인 분석과 규제 보고의 결합으로 특정 지갑이 실사용자와 연결되는 사례가 많습니다. 프라이버시 코인도 한계가 존재합니다.
                        
                                 ## Why
                                    주소-거래 그래프 분석과 규제 데이터 결합으로 가명성이 실명화될 수 있습니다.
                        
                                 ## Sources
                                     - Source 1: Chainalysis Industry Reports.
                                     - Source 2: Narayanan et al., "Bitcoin and Cryptocurrency Technologies" (Princeton).
                        
                        """;

    }


    // 사용자 입력을 최종적으로 문자열로 구성
    protected abstract String userContent(Message message);
}
