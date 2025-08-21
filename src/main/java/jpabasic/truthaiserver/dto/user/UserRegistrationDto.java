package jpabasic.truthaiserver.dto.user;

import jpabasic.truthaiserver.domain.User;
import jpabasic.truthaiserver.domain.UserBaseInfo;
import jpabasic.truthaiserver.domain.UserRole;
import lombok.Data;

@Data
public class UserRegistrationDto {
    private String email;
    private String nickname;
    private String profilePictureUrl;
    private UserRole userRole;

    public UserRegistrationDto(String email, String nickname, String profilePictureUrl){
        this.email=email;
        this.nickname=nickname;
        this.profilePictureUrl = profilePictureUrl;
        this.userRole = UserRole.USER;
    }

    public User toEntity(){
        UserBaseInfo userBaseInfo = new UserBaseInfo(email, nickname, profilePictureUrl);
        return new User(userBaseInfo, userRole);
    }
}
