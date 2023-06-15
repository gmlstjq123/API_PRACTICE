package com.example.umc4_heron_template_jpa.member;

import com.example.umc4_heron_template_jpa.board.Board ;
import com.example.umc4_heron_template_jpa.utils.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
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
    @Column(nullable = false)
    private String password;
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards;
    public Member createMember(String email, String nickName, String password){
        this.email = email;
        this.nickName= nickName;
        this.password = password;
        return this;
    }

    public void updateNickName(String nickName){
        this.nickName = nickName;
    }
}
