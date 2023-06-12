package com.example.umc4_heron_template_jpa.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostBoardRes {
    private String nickName;
    private String title;
    private String content;
}
