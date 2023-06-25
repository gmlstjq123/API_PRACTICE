package com.example.umc4_heron_template_jpa.member;

import com.example.umc4_heron_template_jpa.board.Board ;
import com.example.umc4_heron_template_jpa.utils.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member extends BaseTimeEntity {
    @Column
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String nickName;
    @Column(nullable = true)
    private String password;
    @Column(nullable = true)
    private String refreshToken;
    @Column(nullable = true)
    private String accessToken;
    @Column(columnDefinition = "boolean default false")
    private boolean isSocialLogin;
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards = new ArrayList<>();
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> comments = new ArrayList<>();
    public Member createMember(String email, String nickName, String password){
        this.email = email;
        this.nickName= nickName;
        this.password = password;
        return this;
    }

    public void updateNickName(String nickName){
        this.nickName = nickName;
    }
    public void updateEmail(String email){
        this.email = email;
    }
    public void updateAccessToken(String accessToken){
        this.accessToken = accessToken;
    }
    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }
    public void updateIsSocialLogin(){
        this.isSocialLogin = true;
    }
}
