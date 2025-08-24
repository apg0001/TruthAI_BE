package jpabasic.truthaiserver.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class SourceService {

    public String extractMainContent(String url) {
        try{
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; TruthAI/1.0")
                    .timeout(5000)
                    .get();

            // 1. article / section / main id 우선 확인
            Elements candidates = doc.select("article, section, main");
            if (candidates.isEmpty()) {
                candidates = doc.select("div");
            }

            Element bestElement = null;
            int maxLength = 0;

            for (Element el : candidates) {
                // 2. HTML 태그를 제외하고 순수 텍스트 길이로 가장 큰 본문 찾기
                String text = el.text().trim();
                int textLength = text.length();

                // 3. 너무 짧은 텍스트 div는 제외 (광고 / 메뉴 등)
                if (textLength > maxLength && textLength > 200) {
                    maxLength = textLength;
                    bestElement = el;
                }
            }

            // 4. 본문 후보 내부에서 <p> 태그 위주로 다시 합치기
            if (bestElement != null) {
                Elements paragraphs = bestElement.select("p");
                if(!paragraphs.isEmpty()){
                    StringBuilder sb = new StringBuilder();
                    for (Element p : paragraphs){
                        sb.append(p.text()).append("\n\n");
                    }
                    return sb.toString().trim();
                }
                return bestElement.text();
            }
            return "null";
        }catch(Exception e){
            log.error("본문 추출 실패 | url={} | error={}", url, e.getMessage());
            return "null";
        }
    }
}
