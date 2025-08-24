//package jpabasic.truthaiserver.service;
//
//import jpabasic.truthaiserver.domain.Source;
//import jpabasic.truthaiserver.dto.answer.openai.ChatGptRequest;
//import jpabasic.truthaiserver.dto.answer.openai.Message;
//import jpabasic.truthaiserver.repository.SourceRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SourceSummaryService {
//
//    private final LlmService llmService;
//    private final SourceRepository sourceRepository;
//
//    /**
//     * Source 본문을 OpenAI API로 요약
//     */
//    public String summarizeSourceContent(Long sourceId) {
//        Optional<Source> sourceOpt = sourceRepository.findById(sourceId);
//        if (sourceOpt.isEmpty()) {
//            throw new IllegalArgumentException("Source not found with id: " + sourceId);
//        }
//
//        Source source = sourceOpt.get();
//        String content = source.getSourceSummary();
//
//        if (content == null || content.isBlank()) {
//            log.warn("Source content is empty for sourceId: {}", sourceId);
//            return "내용이 없습니다.";
//        }
//
//        try {
//            String prompt = createSummaryPrompt(content);
//            String summary = llmService.createGptAnswer(prompt);
//
//            log.info("Source 요약 완료 - sourceId: {}, 원본길이: {}, 요약길이: {}",
//                    sourceId, content.length(), summary.length());
//
//            return summary;
//        } catch (Exception e) {
//            log.error("Source 요약 실패 - sourceId: {}, error: {}", sourceId, e.getMessage());
//            return "요약 생성에 실패했습니다.";
//        }
//    }
//
//    /**
//     * 여러 Source를 일괄 요약
//     */
//    public List<String> summarizeMultipleSources(List<Long> sourceIds) {
//        return sourceIds.stream()
//                .map(this::summarizeSourceContent)
//                .toList();
//    }
//
//    /**
//     * 요약 프롬프트 생성
//     */
//    private String createSummaryPrompt(String content) {
//        return String.format("""
//                다음 텍스트를 한국어로 간결하게 요약해주세요.
//                핵심 내용만 추출하여 2-3문장으로 요약하세요.
//
//                텍스트:
//                %s
//
//                요약:
//                """, content);
//    }
//
//    /**
//     * 구조화된 요약 (JSON 형태)
//     */
//    public String getStructuredSummary(Long sourceId) {
//        Optional<Source> sourceOpt = sourceRepository.findById(sourceId);
//        if (sourceOpt.isEmpty()) {
//            throw new IllegalArgumentException("Source not found with id: " + sourceId);
//        }
//
//        Source source = sourceOpt.get();
//        String content = source.getSourceSummary();
//
//        if (content == null || content.isBlank()) {
//            return "내용이 없습니다.";
//        }
//
//        try {
//            List<Message> messages = List.of(
//                new Message("system", "당신은 텍스트 요약 전문가입니다. 주어진 텍스트를 구조화된 형태로 요약해주세요."),
//                new Message("user", String.format("""
//                    다음 텍스트를 요약해주세요:
//
//                    %s
//
//                    다음 JSON 형태로 응답해주세요:
//                    {
//                        "title": "요약 제목",
//                        "summary": "핵심 내용 요약 (2-3문장)",
//                        "keyPoints": ["주요 포인트1", "주요 포인트2", "주요 포인트3"],
//                        "category": "카테고리 (예: 기술, 과학, 경제 등)"
//                    }
//                    """, content))
//            );
//
//            return llmService.createGptAnswerWithPrompt(messages);
//        } catch (Exception e) {
//            log.error("구조화된 요약 실패 - sourceId: {}, error: {}", sourceId, e.getMessage());
//            return "구조화된 요약 생성에 실패했습니다.";
//        }
//    }
//}
