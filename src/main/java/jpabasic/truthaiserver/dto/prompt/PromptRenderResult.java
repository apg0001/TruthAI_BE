package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.dto.answer.Message;

import java.util.List;

//프롬프트 최종 렌더 결과
public record PromptRenderResult(List<Message> messages, String templateKey) {
}
