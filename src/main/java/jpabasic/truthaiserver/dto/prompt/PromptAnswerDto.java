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

        try {
            String[] parts = response.split("\n\n##\\s*Sources", 2); //## Sources, ##Sources 모두 허용
            if (parts.length > 2) {
                return "[]";
            }

            String text = parts[1].trim();
            JSONArray jsonArray = new JSONArray();

            //줄바꿈 기준으로 나누기
            String[] lines = text.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("-")) {
                    String[] splits = line.split(":", 2);
                    if (splits.length < 2) continue;

                    String name = splits[0].trim();
                    String url = splits[1].trim();

                    JSONObject obj = new JSONObject();
                    obj.put("name", name);
                    obj.put("url", url);
                    jsonArray.add(obj);
                }
            }

            return text;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new BusinessException(ANSWER_RENDER_ERROR);
        }
    }
}
