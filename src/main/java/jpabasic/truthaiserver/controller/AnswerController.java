package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.service.AnswerService;
import jpabasic.truthaiserver.service.prompt.PromptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Tag(name="AI 답변 관련 api")
@RequestMapping("/llm-answer")
public class AnswerController {

    private final AnswerService answerService;
    private final PromptService promptService;

    public AnswerController(AnswerService answerService,PromptService promptService) {
        this.answerService = answerService;
        this.promptService = promptService;
    }


    @PostMapping("/models")
    @Operation(summary="AI 교차검증 > 프롬프트 최적화 없이 유저가 직접 작성한 질문으로 물어볼 때",description = "models 필드는 선택한 모델들(gpt/claude/gemini) 을 리스트로 주세요. ")
    public List<LlmAnswerDto> getLlmAnswer(@RequestBody LlmRequestDto llmRequestDto){

        List<LLMModel> modelEnums= llmRequestDto.toModelEnums();
        String question=llmRequestDto.getQuestion();
        Long userId=llmRequestDto.getUserId();

        List<LlmAnswerDto> result=answerService.getLlmAnswers(modelEnums,question); //LLM 답변 받기
//        promptService.savePrompt(question,result,userId); //프롬프트 저장 ❓

        return result;
    }


}
