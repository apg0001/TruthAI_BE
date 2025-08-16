package jpabasic.truthaiserver.controller;

import jpabasic.truthaiserver.service.SourcesService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
