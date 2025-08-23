package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.answer.AnswerResultDto;
import jpabasic.truthaiserver.dto.answer.LlmNoPromptRequestDto;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.service.AnswerService;
import jpabasic.truthaiserver.service.prompt.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Tag(name = "AI 답변 관련 api")
@RequestMapping("/llm-answer")
public class AnswerController {

    private final AnswerService answerService;
    private final PromptService promptService;

    public AnswerController(AnswerService answerService, PromptService promptService) {
        this.answerService = answerService;
        this.promptService = promptService;
    }


    @PostMapping("/models")
    @Operation(summary = "AI 교차검증 > 프롬프트 최적화 없이 유저가 직접 작성한 질문으로 물어볼 때", description = "models 필드는 선택한 모델들(gpt/claude/gemini) 을 리스트로 주세요. ")
    public ResponseEntity<AnswerResultDto> getLlmAnswer(@RequestBody LlmNoPromptRequestDto request, @AuthenticationPrincipal(expression = "user") User user) {

        List<LLMModel> modelEnums = request.toModelEnums();
        String question = request.question();

        System.out.println("\n\n----------\nLLM 답변\nuserId"+user.getId()+"\n\n==========\n\n");

        //저장 되는 제목 설정 (질문 내용 요약)
        String summary = promptService.summarizePrompts(question);

        List<LlmAnswerDto> dto = answerService.selectAnswer(modelEnums, question); //LLM 답변 받기
        Long promptId=promptService.savePromptAnswer(question, dto, user, summary); // 질문 & 답변 저장

        AnswerResultDto result=new AnswerResultDto(promptId,dto);

        return ResponseEntity.ok(result);

    }


}
