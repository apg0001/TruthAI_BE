package jpabasic.truthaiserver.controller;

import jpabasic.truthaiserver.service.sources.SourcesService;
import org.springframework.stereotype.Controller;

@Controller
public class SourceController {

    private final SourcesService sourcesService;

    public SourceController(SourcesService sourcesService) {
        this.sourcesService = sourcesService;
    }


//    @GetMapping("/test")
//    public ResponseEntity<?> seperateTest(){
//        sourcesService.separateUrl();
//        return ResponseEntity.ok().build();
//    }
}
