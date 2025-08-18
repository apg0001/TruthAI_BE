package jpabasic.truthaiserver.dto.answer.promptdef;


import java.util.List;

public record PromptDef (
        String key,int version, List<String> variables,
        List<String> system, String domain,String fewshot,
        String user
){

    /**
     * @param key : "summary","optimized" 같이 프롬프트의 역할
     * @param variables : [persona, domain,question] 무조건 유저한테 받아야 함 
     * @param user : question ❓ (미정)
     * @return : 새로운 PromptDef 객체 생성 -> 유저가 직접 수정도 할 수 있도록 함. 
     */
    public static PromptDef get(String key, List<String> variables, String user){
        return new PromptDef(key,1,variables,null,null,null,user);
    }
}


