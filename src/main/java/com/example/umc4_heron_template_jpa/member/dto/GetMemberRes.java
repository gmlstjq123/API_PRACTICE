package com.example.umc4_heron_template_jpa.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetMemberRes {
    private Long memberId;
    private String nickName;
    private String email;
    private String password;
}
