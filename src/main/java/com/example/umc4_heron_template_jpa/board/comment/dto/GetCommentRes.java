package com.example.umc4_heron_template_jpa.board.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetCommentRes {
    private String boardTitle;
    // private String reply; // 같은 게시글에 작성한 댓글이 따로 표시되어 불편
    private List<String> reply; // 같은 게시글에 작성한 댓글은 한번에 모아서 출력
}
