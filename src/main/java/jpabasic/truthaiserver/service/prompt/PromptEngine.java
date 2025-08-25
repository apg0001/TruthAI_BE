package jpabasic.truthaiserver.service.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jpabasic.truthaiserver.common.prompt.PromptRegistry;
import jpabasic.truthaiserver.domain.PromptDomain;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiRequestDto;
import jpabasic.truthaiserver.dto.answer.gemini.GeminiResponseDto;
import jpabasic.truthaiserver.dto.prompt.*;
import jpabasic.truthaiserver.dto.prompt.adapter.ClaudeAdapter;
import jpabasic.truthaiserver.dto.prompt.adapter.GeminiAdapter;
import jpabasic.truthaiserver.dto.prompt.template.BasePromptTemplate;
import jpabasic.truthaiserver.dto.sources.SourcesDto;
import jpabasic.truthaiserver.exception.BusinessException;
import jpabasic.truthaiserver.exception.ErrorMessages;
import jpabasic.truthaiserver.service.LlmService;
import jpabasic.truthaiserver.service.claude.ClaudeService;
import jpabasic.truthaiserver.service.gpt.GptService;
import jpabasic.truthaiserver.service.perplexity.PerplexityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptEngine {

    private final LlmService llmService;
    private final PromptRegistry registry;
    private final OptimizedTemplate optimizedTemplate;
    private final GeminiAdapter geminiAdapter;
    private final ClaudeAdapter claudeAdapter;
    private final GptService gptService;
    private final ClaudeService claudeService;
    private final PerplexityService perplexityService;

    @Autowired
    private ObjectMapper objectMapper;

    //persona 없는 경우
    public String execute(String templateKey,String question){
         String result=getOptimizedAnswerByGpt(templateKey,new Message(question),null,null);
        System.out.println("✅ summarize result:"+ result);
        return result;
    }

    //최적화 프롬프트 생성 (new🏃🏃)
    public String execute(String templateKey,String question,String persona,PromptDomain domain){
        return getOptimizedAnswerByGpt(templateKey,new Message(question),persona,domain);
    }


    //최적화 프롬프트 반환(String type)
    public String optimizingPrompt(LlmRequestDto request){
        PromptDomain domain=request.getPromptDomain();
//        PromptDomain promptDomain=PromptDomain.nameOf(domain);
        String persona=request.getPersona();
        Message message=new Message(request.getQuestion());

        return optimizedTemplate.getOptimizedPrompt(domain,persona,message);
    }

    //최적화 프롬프트 반환(List<Message>)
    public List<Message> executeInternal(String templateKey, Message message, @Nullable String persona,@Nullable PromptDomain domain){
        BasePromptTemplate template=registry.getByKey(templateKey);
        System.out.println("🍪template:"+template);
        if(template==null){
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }

        List<Message> result=template.render(message,persona,domain);
        try {
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            System.out.println("🪵 Message List:\n" + prettyJson);
        } catch (JsonProcessingException e) {
            System.out.println("⚠️ Failed to pretty print messages: " + e.getMessage());
        }
        return result; //기능에 맞는 프롬프트 찾아서 실행


    }

    //claude 실행
    public LLMResponseDto getStructuredAnswerByClaude(String templateKey, Message message, @Nullable String persona,@Nullable PromptDomain domain) throws JsonProcessingException {
        BasePromptTemplate template=registry.getByKey(templateKey);
        if(template==null){
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }

        List<Message> result=executeInternal(templateKey,message,persona,domain);
        System.out.println("🍪 여기 까지 성공");

        //Claude 호출 및 structured JSON 결과 파싱
        LLMResponseDto dto=claudeService.structuredWithClaude(result);

        return dto;
    }

    public LLMResponseDto getStructuredAnswerByPerplexity(String templateKey, Message message, @Nullable String persona,@Nullable PromptDomain domain) throws JsonProcessingException {
        BasePromptTemplate template=registry.getByKey(templateKey);
        System.out.println("template:"+template);
        if(template==null){
            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
        }

        List<Message> result=executeInternal(templateKey,message,persona,domain);
        System.out.println("🍪 여기 까지 성공");

        //Perplexity 호출 및 structured JSON 결과 파싱
        LLMResponseDto dto=perplexityService.structuredWithPerplexity(result);

        return dto;
    }


    //gpt 실행
    public String getOptimizedAnswerByGpt(String templateKey, Message message, @Nullable String persona, @Nullable PromptDomain domain){
        //templateKey에 맞는 template 호출 -> gpt에 request 보낼 수 있는 형태로 리턴
        List<Message> result=executeInternal(templateKey,message,persona,domain);
        return gptService.createGptAnswerWithPrompt(result);
    }

    public LLMResponseDto getStructuredAnswerByGpt(String templateKey, Message message, @Nullable String persona, @Nullable PromptDomain domain) throws JsonProcessingException {
        List<Message> result=executeInternal(templateKey,message,persona,domain);
        System.out.println("🤨result:"+result.toString());
        return gptService.structuredWithGpt(result);
    }



    //gemini 실행
    public LLMResponseDto getStructuredAnswerByGemini(String templateKey, Message message, @Nullable String persona, @Nullable PromptDomain domain){

//        //"optimized" 프롬프트 가져오기
//        BasePromptTemplate template=registry.getByKey(templateKey);
//        System.out.println("");
//        if(template==null){
//            throw new BusinessException(ErrorMessages.PROMPT_TEMPLATE_NOT_FOUND);
//        }

        GeminiRequestDto dto=geminiAdapter.toGeminiRequest(message,persona,domain);
        System.out.println("✅request:"+dto);
        GeminiResponseDto result= llmService.geminiClient(dto);

        String answer=result.getCandidates().get(0)
                .getContent()
                .getParts().get(0)
                .getText();
        System.out.println("✅answer:"+answer);

        // List<Web> 반환
        List<GeminiResponseDto.Web> webs =
                Optional.ofNullable(result.getCandidates()).orElse(List.of()).stream()
                        .map(GeminiResponseDto.Candidate::getGroundingMetadata)
                        .filter(Objects::nonNull)
                        .flatMap(gm -> Optional.ofNullable(gm.getGroundingChunks()).orElse(List.of()).stream())
                        .map(GeminiResponseDto.GroundingChunk::getWeb)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        System.out.println("✅ webs: " + webs);


        List<LLMResponseDto.SourceResponseDto> sources=LLMResponseDto.toSourceResponseDto(webs);
        System.out.println("✅sources:"+sources.toString());
        LLMResponseDto response=new LLMResponseDto(answer,sources);
        return response;
    }



    //optimized prompt 반환
    List<Message> getOptimizedPrompt(String templateKey, OptPromptRequestDto request){
        Message message=new Message(request.getQuestion());
        String persona=request.getPersona();
        PromptDomain domain=request.getPromptDomain();

        List<Message> result=executeInternal(templateKey,message,persona,domain);
        return result;
    }

}
