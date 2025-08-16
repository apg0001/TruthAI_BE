package jpabasic.truthaiserver.repository;

import jpabasic.truthaiserver.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folder, Long> {
}
