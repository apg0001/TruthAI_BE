package jpabasic.truthaiserver.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.HashMap;
import java.util.Map;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PromptDomain {
    POLITICS("정치"),
    ECONOMICS("경제"),
    SOCIAL("사회"),
    CULTURE("문화"),
    SCIENCE("IT,과학"),
    INTERNATIONAL("국제"),
    CLIMATE("재난,기후,환경"),
    LIFE("생활,건강"),
    SPORTS("스포츠"),
    ENTERTAINMENT("연예"),
    WEATHER("날씨"),
    ELSE("기타");

    final private String name;

    private static final Map<String,PromptDomain> KOREAN_TO_ENUM=new HashMap<>();
    private PromptDomain(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static PromptDomain nameOf(String name) {
        for(PromptDomain domain : PromptDomain.values()) {
            if(domain.name().equals(name)) {
                return domain;
            }
        }
        return null;
    }
}
