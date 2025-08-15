package jpabasic.truthaiserver.dto.prompt;

import jpabasic.truthaiserver.dto.answer.Message;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.language.bm.Rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//프롬프트 최종 렌더 결과
public record PromptAnswerDto(Long promptId,String answer, List<Map<String,String>> sources) {

    public PromptAnswerDto(Long promptId,String response){
        this(
                promptId,
                parseAnswer(response),
                parseSources(response)
        );
    }

    private static String parseAnswer(String response){
        //##답변 이후 부터 ##Sources 전까지 추출
        String[] parts=response.split("##Sources",2);
        if(parts.length>0){
            return parts[0]
                    .replace("##답변","")
                    .trim();
        }
        return "";
    }

    private static List<Map<String,String>> parseSources(String response){
        List<Map<String,String>> sources=new ArrayList<>();

        //마크다운 링크 패턴 : [링크텍스트] (URL)
        /**
         * - Harvard T.H. Chan School of Public Health. "The Nutrition Source - Carbohydrates." [www.hsph.harvard.edu](https://www.hsph.harvard.edu/nutritionsource/carbohydrates/)
         */
        Pattern pattern= Pattern.compile("-\\s*(.*?)\\s*\\[(.*?)\\]\\((.*?)\\)");
        Matcher matcher=pattern.matcher(response);

        while(matcher.find()){
            Map<String,String> map=new HashMap<>();
            map.put("title",matcher.group(1).trim()); //설명/출처명
            map.put("displayText",matcher.group(2).trim()); //링크 표시 텍스트
            map.put("url",matcher.group(3).trim()); //실제 URL
            sources.add(map);
        }
        System.out.println(sources);
        return sources;
    }
}
