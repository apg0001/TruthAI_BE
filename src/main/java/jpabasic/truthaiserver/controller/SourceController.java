package jpabasic.truthaiserver.controller;

import jpabasic.truthaiserver.service.SourceService;
import jpabasic.truthaiserver.service.sources.SourcesService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
@RequestMapping("/source")
@Controller
@RequiredArgsConstructor
public class SourceController {
    private SourcesService sourcesService;
    @Autowired
    private SourceService sourceService;

//    public SourceController(SourcesService sourcesService) {
//        this.sourcesService = sourcesService;
//    }


//    @GetMapping("/test")
//    public ResponseEntity<?> seperateTest(){
//        sourcesService.separateUrl();
//        return ResponseEntity.ok().build();
//    }
    @PostMapping("/test")
    public ResponseEntity<?> extractByParam(@RequestParam("url") String url) {
        if (!StringUtils.hasText(url) || !isValidUrl(url)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid url parameter"
            ));
        }

        String content = sourceService.extractMainContent(url);
        if (!StringUtils.hasText(content)) {
            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "content", "",
                    "note", "본문을 추출하지 못했습니다."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "url", url,
                "content", content
        ));
    }

    private boolean isValidUrl(String raw) {
        try {
            URI u = new URI(raw);
            return "http".equalsIgnoreCase(u.getScheme()) || "https".equalsIgnoreCase(u.getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
