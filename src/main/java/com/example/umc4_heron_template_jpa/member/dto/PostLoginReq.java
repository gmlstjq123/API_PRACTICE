package com.example.umc4_heron_template_jpa.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostLoginReq {
    private String email;
    private String password;
}
