package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.dto.answer.Message;
import jpabasic.truthaiserver.dto.prompt.LLMResponseDto;
import jpabasic.truthaiserver.dto.prompt.OptPromptRequestDto;
import jpabasic.truthaiserver.dto.prompt.PromptAnswerDto;
import jpabasic.truthaiserver.dto.prompt.PromptResultDto;
import jpabasic.truthaiserver.service.LlmService;
import jpabasic.truthaiserver.service.sources.SourcesService;
import jpabasic.truthaiserver.service.prompt.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name="프롬프트 관련 api")
@RequestMapping("/prompt")
public class PromptController {

    private final PromptService promptService;
    private final LlmService llmService;
    private final SourcesService sourcesService;

    public PromptController(PromptService promptService,LlmService llmService,SourcesService sourcesService)
    {
        this.promptService = promptService;
        this.llmService = llmService;
        this.sourcesService = sourcesService;
    }


    @PostMapping("/create-best")
    @Operation(summary="최적화 프롬프트 생성",description = "templateKey 값은 optimzied로 주세요.")
    public ResponseEntity<Map<String,Object>> savePrompt(@RequestBody OptPromptRequestDto dto, @AuthenticationPrincipal User user){
        Long promptId=promptService.saveOriginalPrompt(dto,user);
        List<Message> optimizedPrompt=promptService.getOptimizedPrompt(dto,promptId);

        Map<String,Object> map=new HashMap<>();
        map.put("optimizedPrompt",optimizedPrompt);
        map.put("promptId",promptId);
        return ResponseEntity.ok(map); //저장된 promptId도 함께 반환.
    }

//    @PostMapping("/get-best")
//    @Operation(summary="최적화 프롬프트를 통해 응답 생성 받기")
//    public ResponseEntity<String> getOptimizedAnswer(@RequestBody LlmRequestDto dto, @AuthenticationPrincipal User user){
//
//        String optimizedPrompt=promptService.optimizingPrompt(dto);
//        return ResponseEntity.ok(optimizedPrompt);
//    }

    @PostMapping("/get-best/organized")
    @Operation(summary="최적화 프롬프트를 통해 응답 생성 받기",description = "gpt, claude, gemini 사용 가능")
    public ResponseEntity<List<Map<LLMModel,PromptResultDto>>> getOrganizedAnswer(@RequestParam Long promptId,
                                                              @RequestBody LlmRequestDto dto,
                                                              @AuthenticationPrincipal User user){

        //최적화 프롬프트 받고 응답 받기
        List<Map<LLMModel,LLMResponseDto>> response=promptService.runByModel(dto);

        //answer,sources 나눠서 응답 받기
//        List<Map<LLMModel,PromptAnswerDto>> seperatedAnswers=llmService.seperateAnswers(promptId,response);

        //응답 내용 저장
        List<Map<LLMModel, PromptResultDto>> result=promptService.saveAnswers(response,user,promptId);

        //정돈된 source로 응답
        return ResponseEntity.ok(result);
    }

    @PostMapping("/summarize")
    @Operation(summary="프롬프트 내용 요약하기",description="model 필드 값은 gpt로 주세요!")
    public ResponseEntity<String> summarizePrompt(@RequestBody LlmRequestDto dto){
        String prompt=dto.getQuestion();
        String result=promptService.summarizePrompts(prompt);
        return ResponseEntity.ok(result);
    }



}
