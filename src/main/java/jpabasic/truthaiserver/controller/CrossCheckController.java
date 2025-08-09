package jpabasic.truthaiserver.controller;

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

    /**
     * 벡터 차원/로드 개수 확인
     */
    @GetMapping("/test")
    public Map<String, Object> similarity(
            @RequestParam("a") String a,
            @RequestParam("b") String b
    ) {
        float[] va = embeddingService.embed(a);
        float[] vb = embeddingService.embed(b);
        double cos = EmbeddingService.cosine(va, vb);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("a", a);
        res.put("b", b);
        res.put("dim", Math.min(va.length, vb.length));
        res.put("cosine", cos);
        return res;
    }
}
