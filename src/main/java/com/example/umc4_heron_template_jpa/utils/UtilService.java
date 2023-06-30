package com.example.umc4_heron_template_jpa.utils;

import com.example.umc4_heron_template_jpa.board.Board;
import com.example.umc4_heron_template_jpa.board.BoardRepository;
import com.example.umc4_heron_template_jpa.board.comment.Comment;
import com.example.umc4_heron_template_jpa.board.comment.CommentRepository;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponseStatus;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UtilService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public Member findByMemberIdWithValidation(Long memberId) throws BaseException {
        Member member = memberRepository.findMemberById(memberId).orElse(null);
        if(member == null) throw new BaseException(BaseResponseStatus.NONE_EXIST_MEMBER);
        return member;
    }

    public Member findByEmailWithValidation(String email) throws BaseException {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if(member == null) throw new BaseException(BaseResponseStatus.POST_USERS_NONE_EXISTS_EMAIL);
        return member;
    }

    public Board findByBoardIdWithValidation(Long boardId) throws BaseException {
        Board board = boardRepository.findBoardById(boardId).orElse(null);
        if(board == null) throw new BaseException(BaseResponseStatus.NONE_EXIST_BOARD);
        return board;
    }
}
