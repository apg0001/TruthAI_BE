package jpabasic.truthaiserver.dto.folder;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFolderRequest {
    @NotNull
    private String folderName;
    @NotNull
    private String folderType;
}
