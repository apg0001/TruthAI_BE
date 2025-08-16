package jpabasic.truthaiserver.service;

import jakarta.transaction.Transactional;
import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.domain.Folder;
import jpabasic.truthaiserver.dto.CreateFolderRequest;
import jpabasic.truthaiserver.repository.FolderRepository;
import jpabasic.truthaiserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createFolder(CreateFolderRequest request){
        User user = userRepository.findById(request.getUserID())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + request.getUserID()));

        Folder folder = new Folder();
        folder.assignUser(user);

        Folder saved = folderRepository.save(folder);
        return saved.getId();

    }
}
