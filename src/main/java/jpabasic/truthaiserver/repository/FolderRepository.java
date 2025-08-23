package jpabasic.truthaiserver.repository;

import jpabasic.truthaiserver.domain.Folder;
import jpabasic.truthaiserver.domain.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// FolderRepository.java
public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByUserId(Long userId);
    @Query("SELECT f FROM Folder f WHERE f.user.id = :userId AND f.type = :folderType")
    List<Folder> findFoldersByUserIdAndType(@Param("userId") Long userId, @Param("folderType") String folderType);
}