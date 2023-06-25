package com.example.umc4_heron_template_jpa.board.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostCommentRes {
    private String nickName;
    private String reply;
}
