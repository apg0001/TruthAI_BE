//package jpabasic.truthaiserver.controller;
//
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
//import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
//import jpabasic.truthaiserver.dto.answer.LlmRequestDto;
//import jpabasic.truthaiserver.service.AnswerService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/llm-answer")
//@RequiredArgsConstructor
//public class AnswerController {
//
//    private final AnswerService answerService;
//
//    @PostMapping("/models")
//    public List<LlmAnswerDto> getLlmAnswer(@RequestBody LlmRequestDto llmRequestDto){
//        return answerService.getLlmAnswer(llmRequestDto);
//    }
//}
