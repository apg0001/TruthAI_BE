package jpabasic.truthaiserver.exception;

public class ErrorMessages {
    public static final String Invalid_Token = "유효하지 않은 토큰입니다.";
    public static final String REGISTRATION_FAILED = "사용자 등록에 실패했습니다."; // ✅ 이 줄 추가
    public static final String USER_NULL_ERROR="해당 유저를 찾을 수 없어요.";
    
    //llm answer 관련
    public static final String LLM_MODEL_ERROR="지원하지 않는 모델입니다.";
    public static final String LLM_NULL_ERROR="선택된 모델이 없습니다.";
    public static final String ENUM_ERROR="ENUM 값이 제대로 파싱되지 않습니다.";
    public static final String MESSAGE_NULL_ERROR="전달 받은 답변이 없어요.";
    public static final String ANSWER_RENDER_ERROR="AI 답변 파싱 과정에서 오류가 발생했어요.";



    //prompt 관련
    public static final String PROMPT_GENERATE_ERROR="프롬프트 저장에 문제가 생겼어요.";
    public static final String PROMPT_TEMPLATE_NOT_FOUND="해당 프롬프트는 저장소에 있지 않아요.";
    public static final String PROMPT_NOT_FOUND="해당 질문을 찾을 수 없어요.";

    //souce 관련
    public static final String SOURCE_URL_EMPTY="출처의 URL이 없어요.";
    public static final String SAVE_SOURCE_ERROR="Source 저장 과정에서 오류가 발생했어요.";







}