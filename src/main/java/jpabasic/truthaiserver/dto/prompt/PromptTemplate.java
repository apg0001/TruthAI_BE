package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.Message;

import java.util.List;

public interface PromptTemplate {

    String key(); //프롬프트 용도 //summarize 등등 ..

    //동적 변수(맵) 받아서 messages 구성
//    PromptRenderResult render(Map<String,Object> vars);

    List<Message> render(Message message, String persona, PromptDomain domain);

//    //프롬프트 최적화
//    List<Message> render(Message message, String persona, PromptDomain domain);

    List<Message> render(Message message);
}
