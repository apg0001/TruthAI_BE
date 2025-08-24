package jpabasic.truthaiserver.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AdvancedWebScrapingService {

    private static final int TIMEOUT = 10000;
    private static final int MAX_CONTENT_LENGTH = 50000; // 50KB 제한
    private static final int MIN_CONTENT_LENGTH = 100;   // 최소 100자
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 고도화된 본문 추출
     */
    public ScrapingResult extractContent(String url) {
        try {
            // 1. URL 유효성 검사
            if (!isValidUrl(url)) {
                return ScrapingResult.error("유효하지 않은 URL입니다.", "INVALID_URL");
            }

            // 2. 웹페이지 접근 및 파싱
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(TIMEOUT)
                    .followRedirects(true)
                    .get();

            // 3. 메타데이터 추출
            String title = extractTitle(doc);
            String description = extractDescription(doc);
            String language = extractLanguage(doc);
            
            // 4. 본문 추출 (개선된 알고리즘)
            String mainContent = extractMainContentAdvanced(doc);
            
            // 5. 품질 검증
            if (mainContent.length() < MIN_CONTENT_LENGTH) {
                return ScrapingResult.error("충분한 본문을 추출할 수 없습니다.", "INSUFFICIENT_CONTENT");
            }

            // 6. 본문 정제
            String cleanedContent = cleanContent(mainContent);
            
            return ScrapingResult.success(url, title, description, language, cleanedContent);
            
        } catch (IOException e) {
            log.error("웹페이지 접근 실패: {} - {}", url, e.getMessage());
            return ScrapingResult.error("웹페이지에 접근할 수 없습니다: " + e.getMessage(), "ACCESS_FAILED");
        } catch (Exception e) {
            log.error("본문 추출 중 오류 발생: {} - {}", url, e.getMessage());
            return ScrapingResult.error("본문 추출에 실패했습니다: " + e.getMessage(), "EXTRACTION_FAILED");
        }
    }

    /**
     * 개선된 본문 추출 알고리즘
     */
    private String extractMainContentAdvanced(Document doc) {
        // 1. 우선순위 기반 선택자
        String[] selectors = {
            "article", "main", "[role='main']", 
            ".content", ".post-content", ".article-content", ".entry-content",
            ".post-body", ".article-body", ".entry-body",
            "section", ".main-content", ".primary-content"
        };

        Element bestElement = null;
        double bestScore = 0.0;

        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            for (Element element : elements) {
                double score = calculateContentScore(element);
                if (score > bestScore) {
                    bestScore = score;
                    bestElement = element;
                }
            }
        }

        // 2. 후보가 없으면 div 기반 추출
        if (bestElement == null) {
            bestElement = findBestDiv(doc);
        }

        if (bestElement == null) {
            return doc.body() != null ? doc.body().text() : "";
        }

        // 3. 본문 내부 정제
        return extractCleanText(bestElement);
    }

    /**
     * 콘텐츠 품질 점수 계산
     */
    private double calculateContentScore(Element element) {
        if (element == null) return 0.0;

        // 텍스트 길이 점수 (너무 짧거나 긴 것은 제외)
        String text = element.text().trim();
        int textLength = text.length();
        
        if (textLength < MIN_CONTENT_LENGTH || textLength > MAX_CONTENT_LENGTH) {
            return 0.0;
        }

        // 문단 수 점수
        int paragraphCount = element.select("p").size();
        double paragraphScore = Math.min(paragraphCount * 0.1, 2.0);

        // 링크 밀도 점수 (너무 많은 링크는 광고일 가능성)
        int linkCount = element.select("a").size();
        double linkDensity = (double) linkCount / textLength * 1000;
        double linkScore = Math.max(0, 1.0 - linkDensity);

        // HTML 태그 밀도 점수 (순수 텍스트가 많을수록 좋음)
        double htmlDensity = (double) element.html().length() / textLength;
        double htmlScore = Math.max(0, 2.0 - htmlDensity);

        return textLength * 0.001 + paragraphScore + linkScore + htmlScore;
    }

    /**
     * 최적의 div 찾기
     */
    private Element findBestDiv(Document doc) {
        Elements divs = doc.select("div");
        Element bestDiv = null;
        double bestScore = 0.0;

        for (Element div : divs) {
            // 너무 작은 div 제외
            if (div.text().trim().length() < MIN_CONTENT_LENGTH) continue;
            
            // 광고/메뉴 관련 클래스 제외
            String className = div.className().toLowerCase();
            if (className.contains("ad") || className.contains("menu") || 
                className.contains("nav") || className.contains("sidebar")) {
                continue;
            }

            double score = calculateContentScore(div);
            if (score > bestScore) {
                bestScore = score;
                bestDiv = div;
            }
        }

        return bestDiv;
    }

    /**
     * 정제된 텍스트 추출
     */
    private String extractCleanText(Element element) {
        if (element == null) return "";

        // 불필요한 요소 제거
        element.select("script, style, nav, header, footer, .ad, .advertisement, .sidebar").remove();
        
        // 문단별로 텍스트 추출
        Elements paragraphs = element.select("p, h1, h2, h3, h4, h5, h6, li");
        if (paragraphs.isEmpty()) {
            return element.text().trim();
        }

        StringBuilder sb = new StringBuilder();
        for (Element p : paragraphs) {
            String text = p.text().trim();
            if (text.length() > 10) { // 너무 짧은 문단 제외
                sb.append(text).append("\n\n");
            }
        }

        return sb.toString().trim();
    }

    /**
     * 메타데이터 추출
     */
    private String extractTitle(Document doc) {
        Element titleElement = doc.select("title").first();
        if (titleElement != null) {
            return titleElement.text().trim();
        }
        
        Element ogTitle = doc.select("meta[property=og:title]").first();
        if (ogTitle != null) {
            return ogTitle.attr("content").trim();
        }
        
        return "";
    }

    private String extractDescription(Document doc) {
        Element descElement = doc.select("meta[name=description]").first();
        if (descElement != null) {
            return descElement.attr("content").trim();
        }
        
        Element ogDesc = doc.select("meta[property=og:description]").first();
        if (ogDesc != null) {
            return ogDesc.attr("content").trim();
        }
        
        return "";
    }

    private String extractLanguage(Document doc) {
        Element langElement = doc.select("html[lang]").first();
        if (langElement != null) {
            return langElement.attr("lang");
        }
        
        Element metaLang = doc.select("meta[http-equiv=content-language]").first();
        if (metaLang != null) {
            return metaLang.attr("content");
        }
        
        return "ko"; // 기본값
    }

    /**
     * URL 유효성 검사
     */
    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return url.startsWith("http://") || url.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 콘텐츠 정제
     */
    private String cleanContent(String content) {
        if (content == null) return "";
        
        return content
                .replaceAll("\\s+", " ")           // 연속 공백 제거
                .replaceAll("\\n\\s*\\n", "\n\n")  // 빈 줄 정리
                .trim();
    }

    /**
     * 비동기 크롤링 (여러 URL 동시 처리)
     */
    public List<ScrapingResult> extractMultipleContents(List<String> urls) {
        List<CompletableFuture<ScrapingResult>> futures = urls.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> extractContent(url), executorService))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();
    }

    /**
     * 크롤링 결과 DTO
     */
    public static class ScrapingResult {
        private final boolean success;
        private final String url;
        private final String title;
        private final String description;
        private final String language;
        private final String content;
        private final String errorMessage;
        private final String errorCode;

        private ScrapingResult(boolean success, String url, String title, String description, 
                             String language, String content, String errorMessage, String errorCode) {
            this.success = success;
            this.url = url;
            this.title = title;
            this.description = description;
            this.language = language;
            this.content = content;
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }

        public static ScrapingResult success(String url, String title, String description, 
                                          String language, String content) {
            return new ScrapingResult(true, url, title, description, language, content, null, null);
        }

        public static ScrapingResult error(String errorMessage, String errorCode) {
            return new ScrapingResult(false, null, null, null, null, null, errorMessage, errorCode);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getUrl() { return url; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getLanguage() { return language; }
        public String getContent() { return content; }
        public String getErrorMessage() { return errorMessage; }
        public String getErrorCode() { return errorCode; }
    }

    /**
     * 서비스 종료 시 리소스 정리
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
