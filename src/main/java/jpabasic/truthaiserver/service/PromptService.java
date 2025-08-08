package jpabasic.truthaiserver.service;

import jpabasic.truthaiserver.domain.Answer;
import jpabasic.truthaiserver.domain.Prompt;
import jpabasic.truthaiserver.dto.answer.LlmAnswerDto;
import jpabasic.truthaiserver.repository.PromptRepository;
import jpabasic.truthaiserver.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PromptService {

    private final PromptRepository promptRepository;

    public PromptService(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }

    //summary 생성해야 (생성형 ai 이용)
    public void savePrompt(String question, List<LlmAnswerDto> results, Long userId) {

        //LlmAnswerDto -> Answer
        List<Answer> answers = results.stream()
                .map(dto -> dto.toEntity())
                .toList();

        Prompt prompt = new Prompt(question, answers);
        promptRepository.save(prompt);
    }
}
