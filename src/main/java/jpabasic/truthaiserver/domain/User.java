package jpabasic.truthaiserver.domain;

import jakarta.persistence.*;
import jpabasic.truthaiserver.domain.BaseEntity;
import jpabasic.truthaiserver.domain.UserBaseInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private UserBaseInfo userBaseInfo;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public User(UserBaseInfo userBaseInfo, UserRole userRole){
        this.userBaseInfo = userBaseInfo;
        this.userRole = userRole;
    }
}