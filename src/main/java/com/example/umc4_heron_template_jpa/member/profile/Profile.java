package com.example.umc4_heron_template_jpa.member.profile;

import com.example.umc4_heron_template_jpa.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileId;
    private String profileUrl; // 프로필 사진 URL
    private String profileFileName; // 프로필 사진명

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    //==객체 생성 메서드==//
    public void setMember(Member member){
        this.member = member;
    }
}