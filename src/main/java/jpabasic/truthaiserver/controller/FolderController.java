package jpabasic.truthaiserver.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.dto.CreateFolderRequest;
import jpabasic.truthaiserver.dto.CreateFolderResponse;
import jpabasic.truthaiserver.dto.FolderSummaryResponse;
import jpabasic.truthaiserver.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    // 폴더 조회
    @GetMapping
    @Operation(summary = "폴더 목록 조회")
    public List<FolderSummaryResponse> list(@AuthenticationPrincipal User user) {
        return folderService.listFolders(user);
    }

    // 프롬프트를 폴더에 저장
    @PutMapping("/{folderID}/prompts/{promptId}")
    @Operation(summary = "프롬프트 폴더 저장/이동", description = "promptId의 프롬프트를 folderId 폴더로 이동/저장합니다.")
    public void movePrompt(@PathVariable Long folderID,
                           @PathVariable Long promptId,
                           @AuthenticationPrincipal User user){
        folderService.movePromptToFolder(folderID, promptId, user);
    }
}
