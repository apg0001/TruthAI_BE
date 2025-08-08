package jpabasic.truthaiserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jpabasic.truthaiserver.domain.LLMModel;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
import jpabasic.truthaiserver.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/llm-answer")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/models")
    public List<LlmAnswerDto> getLlmAnswer(@RequestBody LlmRequestDto llmRequestDto){
        System.out.println("🏃 models:"+llmRequestDto.getModels());
        System.out.println("🏃 question:"+llmRequestDto.getQuestion());

        List<LLMModel> modelEnums= llmRequestDto.toModelEnums();
        String question=llmRequestDto.getQuestion();
        return answerService.getLlmAnswers(modelEnums,question);
    }

    @PostMapping("/test")
    public String test(@RequestBody String hello){
        return hello;
    }

    @PostMapping("/llm-debug")
    public ResponseEntity<?> debug(HttpServletRequest request) throws IOException {
        String body = request.getReader().lines().collect(Collectors.joining());
        System.out.println("📦 RAW JSON: [" + body + "]");  // <-- 반드시 []로 감싸서 로그 찍기

        if (body == null || body.isBlank()) {
            return ResponseEntity.badRequest().body("❌ 요청 바디가 비어 있음!");
        }

        ObjectMapper mapper = new ObjectMapper();
        LlmRequestDto dto = mapper.readValue(body, LlmRequestDto.class);

        System.out.println("✅ models: " + dto.getModels());
        System.out.println("✅ question: " + dto.getQuestion());

        return ResponseEntity.ok("ok");
    }

}
