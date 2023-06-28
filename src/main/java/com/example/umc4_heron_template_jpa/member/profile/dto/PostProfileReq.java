package com.example.umc4_heron_template_jpa.member.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter // 해당 클래스에 대한 접근자 생성
@Setter // 해당 클래스에 대한 설정자 생성
@AllArgsConstructor // 해당 클래스의 모든 멤버 변수(userIdx, nickname)를 받는 생성자를 생성
public class PostProfileReq {
    private String profileImgUrl;
    private String profileImgFileName;
    @Size(max=20, message = "한 줄 소개의 길이는 20글자이내로만 입력 가능합니다.")
    private String introduction;
}

