package com.example.umc4_heron_template_jpa.login.dto;

import com.example.umc4_heron_template_jpa.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequestDTO {
    private String email;

    @Builder
    public MemberUpdateRequestDTO(String email){
        this.email = email;
    }

    public Member toEntity(){
        return Member.builder().email(email).build();
    }


}

