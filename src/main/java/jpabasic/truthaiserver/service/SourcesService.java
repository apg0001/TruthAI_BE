package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.Source;
import jpabasic.truthaiserver.dto.prompt.PromptAnswerDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.SourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static jpabasic.truthaiserver.exception.ErrorMessages.SOURCE_URL_EMPTY;

@Service
@Slf4j
public class SourcesService {

    private final SourceRepository sourceRepository;
    private final AnswerService answerService;

    public SourcesService(SourceRepository sourceRepository,AnswerService answerService) {
        this.sourceRepository = sourceRepository;
        this.answerService = answerService;
    }

    //Sources 분리
    public List<Map<String,String>> separateUrl(PromptAnswerDto dto) {
        // 예시 입력(실사용 시 dto.sources() 등으로 대체)
        String sourceText=dto.sources();
//        String sourceText = "Sources\n" +
//                " - 한국경제TV, \"롤드컵 치르는 중인 LoL개미, 침착맨 티빙으로 영상 제공\", https://www.hankyung.com/entertainment/article/201910309907q\n" +
//                " - KBS, \"2년만에 돌아온 '침착맨'...본명은 유준상, 백업 중에 입 닫은 이유는?\", https://entertain.v.daum.net/v/20200818132311568";

        List<Map<String, String>> results = new ArrayList<>();

        // 줄 단위로 자르기 (CR/LF 모두 대응)
        String[] lines = sourceText.split("\\r?\\n");
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (line.equalsIgnoreCase("Sources")) continue; // 헤더 스킵
            if (line.startsWith("-")) line = line.substring(1).trim(); // 불릿 제거

            int idx = line.indexOf("http"); // URL 시작 지점
            if (idx == -1) {
                // URL이 없다면 비즈니스 규칙에 따라 스킵 or 예외
                return Collections.emptyList();
                // continue;  // 스킵하고 싶으면 예외 대신 이 줄 사용
            }

            String title = line.substring(0, idx).trim();
            String url   = line.substring(idx).trim();

            // URL 뒤에 붙을 수 있는 꼬리문자 정리(선택)
            url = url.replaceAll("[)\\]\\}>\"',.]+$", "");

            // title 앞/뒤의 잔여 콤마/공백 정리(선택)
            title = title.replaceAll("^,\\s*|\\s*,\\s*$", "");

            Map<String, String> map = new HashMap<>();
            map.put("title", title);
            map.put("url", url);
            results.add(map);

            // 확인용 로그
            System.out.println("title = " + title);
            System.out.println("url   = " + url);
        }

        // 결과 리스트 사용
        System.out.println("results = " + results);
        return results;
    }



    //Sources 저장
    public void saveSources(PromptAnswerDto dto) {

        Answer answer=answerService.getAnswer(dto);

        List<Map<String, String>> result = separateUrl(dto);
        for (Map<String, String> source : result) {
            String title = source.get("title");
            String url = source.get("url");

            System.out.println("title = " + title);
            System.out.println("url = " + url);

            try {
                Source newSource = new Source(title, url,answer);
                sourceRepository.save(newSource);
            } catch (Exception e) {
                throw new BusinessException("SAVE_SOURCE_ERROR");
            }
        }
    }



}
