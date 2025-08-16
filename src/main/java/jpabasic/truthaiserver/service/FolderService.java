package jpabasic.truthaiserver.service;

import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.domain.Folder;
import jpabasic.truthaiserver.dto.CreateFolderRequest;
import jpabasic.truthaiserver.dto.FolderSummaryResponse;
import jpabasic.truthaiserver.repository.FolderRepository;
import jpabasic.truthaiserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    // 폴더 생성
    @Transactional
    public Long createFolder(CreateFolderRequest request, User user) {
        Folder folder = new Folder();
        folder.assignUser(user);
        folder.rename(request.getFolderName());

        Folder saved = folderRepository.save(folder);
        return saved.getId();
    }

    // 폴더 목록 조회
    @Transactional
    public List<FolderSummaryResponse> listFolders(User user) {
        Long userId = user.getId();
        return folderRepository.findByUserId(userId).stream()
                .map(f -> new FolderSummaryResponse(
                        f.getId(),
                        f.getName(),
                        f.getPrompts() == null ? 0 : f.getPrompts().size()
                ))
                .toList();

    }

}