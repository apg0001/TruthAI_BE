package jpabasic.truthaiserver.service.sources;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.Source;
import jpabasic.truthaiserver.dto.prompt.LLMResponseDto;
import jpabasic.truthaiserver.dto.prompt.PromptAnswerDto;
import jpabasic.truthaiserver.dto.sources.SourcesDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.repository.AnswerRepository;
import jpabasic.truthaiserver.repository.SourceRepository;
import jpabasic.truthaiserver.service.AnswerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static jpabasic.truthaiserver.exception.ErrorMessages.ANSWER_NOT_FOUND;

@Service
@Slf4j
public class SourcesService {

    private final SourceRepository sourceRepository;
    private final AnswerService answerService;
    private final AnswerRepository answerRepository;

    public SourcesService(SourceRepository sourceRepository, AnswerService answerService, AnswerRepository answerRepository) {
        this.sourceRepository = sourceRepository;
        this.answerService = answerService;
        this.answerRepository = answerRepository;
    }

    //Sources 분리
    public List<Map<String,String>> separateUrl(PromptAnswerDto dto) {
        // 예시 입력(실사용 시 dto.sources() 등으로 대체)
        String sourceText=dto.sources();
        if(sourceText==null || sourceText.isEmpty()) return List.of();

        List<Map<String, String>> results = new ArrayList<>();

        // 미리 전체 텍스트 정규화 (개행/공백/따옴표 등 최소 정리)
        String normalized=sourceText
                .replace("\r\n","\n")
                .replace("\r","\n")
                .replace("\t"," ")
                .trim();

        // 줄 단위로 자르기 (CR/LF 모두 대응)
        String[] lines = normalized.split("\n");


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
    public List<SourcesDto> saveSources(LLMResponseDto dto, Long answerId) {


        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ANSWER_NOT_FOUND));

        List<LLMResponseDto.SourceResponseDto> sources=dto.sources();
        List<SourcesDto> sourcesDtos = new ArrayList<>();

        for (LLMResponseDto.SourceResponseDto s : sources) {
            Source source=new Source(s.title(),s.url(),answer);
            sourceRepository.save(source);

            SourcesDto result=SourcesDto.toDto(source);
            sourcesDtos.add(result);
        }

        return sourcesDtos;
    }



}
