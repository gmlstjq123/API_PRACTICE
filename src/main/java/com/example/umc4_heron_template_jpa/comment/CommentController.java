package com.example.umc4_heron_template_jpa.comment;

import com.example.umc4_heron_template_jpa.board.Board;
import com.example.umc4_heron_template_jpa.board.dto.PostBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PostBoardRes;
import com.example.umc4_heron_template_jpa.comment.dto.GetCommentRes;
import com.example.umc4_heron_template_jpa.comment.dto.PostCommentReq;
import com.example.umc4_heron_template_jpa.comment.dto.PostCommentRes;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.INVALID_BOARD_ID;
import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.USERS_EMPTY_USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    /** 댓글 작성하기 **/
    @PostMapping("")
    public BaseResponse<PostCommentRes> addComment(@RequestBody @Validated PostCommentReq postCommentReq) {
        try{
            return new BaseResponse<>(commentService.addComment(postCommentReq));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
    /** 멤버의 댓글 기록 조회 **/
    @GetMapping("/list-up/{member-id}")
    public BaseResponse<List<GetCommentRes>> getComments(@PathVariable(name = "member-id") Long memberId) {
        Member member = memberRepository.findMemberById(memberId);
        if(member == null){
            return new BaseResponse<>(USERS_EMPTY_USER_ID);
        }
        try{
            return new BaseResponse<>(commentService.getComments(memberId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
