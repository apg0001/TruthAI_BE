package jpabasic.truthaiserver.service.prompt;

import jpabasic.truthaiserver.dto.prompt.BasePromptTemplate;
import jpabasic.truthaiserver.dto.answer.Message;
import org.springframework.stereotype.Component;

@Component
public class SummarizeTemplate extends BasePromptTemplate {
    @Override
    protected String userContent(Message message) {
        String text= message.getContent();
        return """
               # 요약 내용
               본문:
               --- 
               %s
               ---
              
                """.formatted(text);
    }

    @Override
    protected String systemIdentity(String domain, String persona) {
        return "";
    }

    @Override
    protected String domainGuidelines(){
        return """
                 # 제목 만들기 가이드
                - 핵심만 10자 내외로 제목 만들기
                - 불필요한 수식어, 접속어 제외시키기
                - 제목만 출력하고 추가 설명 하지 않기
                """;
    }

    @Override
    public String key() {
        return "summarize";
    }
}
