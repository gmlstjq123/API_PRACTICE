package com.example.umc4_heron_template_jpa.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostMemberRes {
    private Long memberId;
    private String nickName;
}
