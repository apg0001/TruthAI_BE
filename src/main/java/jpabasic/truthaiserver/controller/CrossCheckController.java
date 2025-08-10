package jpabasic.truthaiserver.controller;

import jpabasic.truthaiserver.dto.CrossCheckListDto;
import jpabasic.truthaiserver.dto.CrossCheckResponseDto;
import jpabasic.truthaiserver.service.CrossCheckService;
import jpabasic.truthaiserver.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/crosscheck")
@RequiredArgsConstructor
public class CrossCheckController {
    private final CrossCheckService crossCheckService;
    private final EmbeddingService embeddingService;


    @GetMapping("/{promptId}")
    public CrossCheckResponseDto crossCheck(@PathVariable Long promptId) {
        return crossCheckService.crossCheckPrompt(promptId);
    }


    @GetMapping("/list")
    public List<CrossCheckListDto> getCrossCheckList(
            @RequestParam(value = "promptId", required = false) Long promptId
    ) {
        return crossCheckService.getCrossChecklist(promptId);
    }

}
