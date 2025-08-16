package jpabasic.truthaiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.CreateFolderRequest;
import jpabasic.truthaiserver.dto.CreateFolderResponse;
import jpabasic.truthaiserver.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "폴더 관련 api")
@RequestMapping("/folder")
public class FolderController {

    private final FolderService folderService;
    @PostMapping
    @Operation(summary = "폴더 생성", description = "폴더 이름을 전달하면 해당 사용자 소유의 폴더를 생성합니다.")
    public CreateFolderResponse savePromptInFolder(@RequestBody @Valid CreateFolderRequest request, @AuthenticationPrincipal User user) {
        Long id = folderService.createFolder(request, user);
        return new CreateFolderResponse(id);
    }
}
