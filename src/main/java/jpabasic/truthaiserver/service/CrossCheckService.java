package jpabasic.truthaiserver.service;

import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.Claim;
import jpabasic.truthaiserver.domain.Source;
import jpabasic.truthaiserver.dto.CrossCheckResponseDto;
import jpabasic.truthaiserver.dto.LLMResultDto;
import jpabasic.truthaiserver.repository.AnswerRepository;
import jpabasic.truthaiserver.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class CrossCheckService {

    private final AnswerRepository answerRepository;
    private final ClaimRepository claimRepository;
    private final EmbeddingService embeddingService;

    // 임계값(필요하면 yml로 뺄 것)
    private static final double SENTENCE_SIM_THRESHOLD = 0.82;

    @Transactional
    public CrossCheckResponseDto crossCheckPrompt(Long promptId) {
        // 1) 프롬프트의 모델별 답변 수집
        List<Answer> answers = answerRepository.findByPromptId(promptId);
        Map<String, List<String>> modelToSentences = new HashMap<>();

        for (Answer answer : answers) {
            modelToSentences.put(
                    answer.getModel().name(),
                    splitToSentences(answer.getContent())
            );
        }

        // 2) 전체 문장 유니크 셋
        Set<String> universe = getAllUniqueSentences(modelToSentences);

        // 3) 각 모델이 해당 문장을 '포함한다'고 볼지(임베딩 유사도로 판단)
        List<Claim> savedClaims = new ArrayList<>();
        for (String pivot : universe) {
            float[] pivotEmb = embeddingService.embed(pivot);
            for (Answer answer : answers) {
                boolean contains = modelToSentences.get(answer.getModel().name())
                        .stream()
                        .anyMatch(s -> {
                            double sim = EmbeddingService.cosine(pivotEmb, embeddingService.embed(s));
                            return sim >= SENTENCE_SIM_THRESHOLD;
                        });
                if (contains) {
                    Claim claim = new Claim(pivot, 1.0f, answer);
                    savedClaims.add(claimRepository.save(claim));
                }
            }
        }

        // 4) 점수/의견 산출
        List<LLMResultDto> resultList = new ArrayList<>();
        for (String model : modelToSentences.keySet()) {
            double score = calculateScore(model, savedClaims);
            double validUrlRatio = evalSource(savedClaims);
            String opinion = generateOpinion(score, validUrlRatio);
            resultList.add(new LLMResultDto(model, opinion, score));
        }

        return new CrossCheckResponseDto(promptId, resultList);
    }

    /** 한국어까지 고려한 간단한 문장 분리 */
    private List<String> splitToSentences(String content) {
        if (content == null || content.isBlank()) return List.of();
        // 마침표/물음표/느낌표/줄바꿈/한중일 마침표 포함
        String[] arr = content.split("(?<=[.!?。！？…])\\s+|\\n+");
        List<String> out = new ArrayList<>();
        for (String s : arr) {
            String t = s.trim();
            if (t.length() >= 2) out.add(t);
        }
        return out;
    }

    private Set<String> getAllUniqueSentences(Map<String, List<String>> map){
        return map.values().stream().flatMap(List::stream).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** URL 유효성 평가 (중복 제거, HEAD 시도) */
    private double evalSource(List<Claim> claims) {
        Set<String> urls = new LinkedHashSet<>();
        for (Claim c : claims) {
            List<Source> sources = c.getSources();
            if (sources == null) continue;
            for (Source s : sources) {
                if (s.getSourceUrl() != null && !s.getSourceUrl().isBlank()) {
                    urls.add(s.getSourceUrl().trim());
                }
            }
        }
        if (urls.isEmpty()) return 0.0;

        long ok = 0;
        for (String u : urls) {
            if (isReachableURL(u)) ok++;
        }
        return ok / (double) urls.size();
    }

    private boolean isReachableURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int code = conn.getResponseCode();
            return (code >= 200 && code < 400);
        } catch (Exception e) {
            return false;
        }
    }

    /** 간단 가중치: 해당 모델이 포함한 Claim 개수 / 모든 모델 수 */
    private double calculateScore(String model, List<Claim> claims) {
        long modelCount = claims.stream()
                .filter(c -> c.getAnswer() != null && c.getAnswer().getModel().name().equals(model))
                .count();
        long modelKinds = claims.stream()
                .map(c -> c.getAnswer().getModel().name())
                .distinct()
                .count();
        if (modelKinds == 0) return 0.0;
        // 필요하면 universe 문장 수 대비 비율로 바꿔도 됨
        return modelCount / (double) modelKinds;
    }

    private String generateOpinion(double score, double validUrlRatio) {
        StringBuilder sb = new StringBuilder();
        if (score >= 1.0) sb.append("완벽");
        else if (score >= 0.8) sb.append("환각 의심 낮음");
        else if (score >= 0.5) sb.append("환각 의심 높음, 정확도 낮음");
        else sb.append("환각 가능성 높음");

        if (validUrlRatio >= 1.0) sb.append(" | 모든 출처 유효");
        else if (validUrlRatio >= 0.7) sb.append(" | 다수 출처 유효");
        else if (validUrlRatio > 0.0) sb.append(" | 유효하지 않은 출처 포함");
        else sb.append(" | 출처 없음/전부 무효");

        return sb.toString();
    }
}