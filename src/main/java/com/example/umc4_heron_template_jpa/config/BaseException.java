package com.example.umc4_heron_template_jpa.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BaseException extends Exception {
    private BaseResponseStatus status; //BaseResponseStatus 객체에 매핑
}
