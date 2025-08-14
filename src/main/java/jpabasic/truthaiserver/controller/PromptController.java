package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.service.prompt.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Tag(name="프롬프트 관련 api")
@RequestMapping("/prompt")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @PostMapping("/create-best")
    @Operation(summary="최적화 프롬프트 생성")
    public ResponseEntity<String> savePrompt(@RequestBody LlmRequestDto dto){

        String optimizedPrompt=promptService.getOptimizedPrompt(dto);
        return ResponseEntity.ok(optimizedPrompt);
    }

    @PostMapping("/get-best")
    @Operation(summary="최적화 프롬프트를 통해 응답 생성 받기")
    public ResponseEntity<String> getOptimizedAnswer(@RequestBody LlmRequestDto dto){

        String optimizedPrompt=promptService.optimizingPrompt(dto);
        return ResponseEntity.ok(optimizedPrompt);
    }

    @PostMapping("/summarize")
    @Operation(summary="프롬프트 내용 요약하기",description="model 필드 값은 gpt로 주세요!")
    public ResponseEntity<String> summarizePrompt(@RequestBody LlmRequestDto dto){
        String prompt=dto.getQuestion();
        String result=promptService.summarizePrompts(prompt);
        return ResponseEntity.ok(result);
    }



}
