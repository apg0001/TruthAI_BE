package jpabasic.truthaiserver.dto.user;

import lombok.Data;

@Data
public class GoogleInfoDto {

    private String email;
    private String nickname;
    private String profilePictureUrl;

    public GoogleInfoDto(String email, String nickname, String profilePictureUrl){
        this.email = email;
        this.nickname = nickname;
        this.profilePictureUrl = profilePictureUrl;
    }

    public UserRegistrationDto toUserRegisterDto(){
        return new UserRegistrationDto(email, nickname, profilePictureUrl);
    }
}
