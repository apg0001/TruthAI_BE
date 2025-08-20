package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.exception.BusinessException;
import lombok.AllArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.codec.language.bm.Rule;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jpabasic.truthaiserver.exception.ErrorMessages.ANSWER_RENDER_ERROR;

//프롬프트 최종 렌더 결과
public record PromptAnswerDto(Long promptId,String answer, String sources) {

    public PromptAnswerDto(Long promptId,String response){
        this(
                promptId,
                parseAnswer(response),
                parseSources(response)
        );
    }

    private static final Pattern URL_PATTERN =
            Pattern.compile("(https?://[^\\s)]+)");

    private static String parseAnswer(String response){
        //##답변 이후 부터 ##Sources 전까지 추출
        String[] parts=response.split("## Sources",2);
        System.out.println("✅parts"+ Arrays.stream(parts).toList());

        System.out.println("✅ parts[0]:"+parts[0]);
        System.out.println("✅ parts[1]:"+parts[1]);
        return parts[0];
    }

    private static String parseSources(String response) {
        if (response == null) return "[]";

        // 1) "Sources" 섹션 시작 지점 찾기 (## Sources / ##Sources / **Sources:** 등)
        Pattern header = Pattern.compile("(?im)^\\s*(##\\s*Sources\\s*|\\*\\*Sources:?\\*\\*)\\s*$");
        Matcher hm = header.matcher(response);
        if (!hm.find()) return "[]"; // 섹션 못 찾음

        String text = response.substring(hm.end()).trim();

        // 2) 라인 단위 파싱: "- <name>[: ...]" 라인을 만나면 name을 기억하고,
        //    그 다음 라인들에서 첫 번째 URL을 찾아 매칭
        JSONArray jsonArray = new JSONArray();
        String[] lines = text.split("\\R"); // 모든 개행 대응
        String pendingName = null;

        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;

            // 섹션 종료(다음 헤더) 신호가 보이면 중단 (옵션)
            if (line.startsWith("##")) break;

            // 2-1) 새 bullet 시작
            if (line.startsWith("-")) {
                // "- " 제거
                String name = line.replaceFirst("^-\\s*", "");

                // "이름: 제목" 형태면 콜론 앞까지만 이름으로 사용 (제목은 버림)
                int colonIdx = name.indexOf(':');
                if (colonIdx >= 0) {
                    name = name.substring(0, colonIdx).trim();
                }
                pendingName = name;
                // 같은 줄에 URL이 이미 있으면 그 자리에서 처리
                Matcher m = URL_PATTERN.matcher(line);
                if (m.find()) {
                    addSource(jsonArray, pendingName, m.group());
                    pendingName = null;
                }
                continue;
            }

            // 2-2) bullet 다음 줄에서 URL 찾기
            if (pendingName != null) {
                Matcher m = URL_PATTERN.matcher(line);
                if (m.find()) {
                    addSource(jsonArray, pendingName, m.group());
                    pendingName = null;
                    continue;
                }

                // 마크다운 링크 형태: [텍스트](URL)
                int l = line.indexOf('(');
                int r = line.indexOf(')', l + 1);
                if (l >= 0 && r > l) {
                    String maybeUrl = line.substring(l + 1, r);
                    if (maybeUrl.startsWith("http")) {
                        addSource(jsonArray, pendingName, maybeUrl);
                        pendingName = null;
                    }
                }
            }
        }

        return jsonArray.toJSONString();
    }

    private static void addSource(JSONArray array, String name, String url) {
        JSONObject obj = new JSONObject();
        obj.put("name", name == null ? "" : name.trim());
        obj.put("url", url == null ? "" : url.trim());
        array.add(obj);
    }
}
