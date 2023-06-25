package com.example.umc4_heron_template_jpa.board.comment;

import com.example.umc4_heron_template_jpa.board.comment.dto.GetCommentRes;
import com.example.umc4_heron_template_jpa.board.comment.dto.PostCommentReq;
import com.example.umc4_heron_template_jpa.board.comment.dto.PostCommentRes;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.login.jwt.JwtService;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import com.example.umc4_heron_template_jpa.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.USERS_EMPTY_USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final UtilService utilService;
    private final JwtService jwtService;

    /** 댓글 작성하기 **/
    @PostMapping("")
    public BaseResponse<PostCommentRes> addComment(@RequestBody @Validated PostCommentReq postCommentReq) {
        try{
            Long memberId = jwtService.getMemberIdx();
            return new BaseResponse<>(commentService.addComment(memberId, postCommentReq));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
    /** 멤버의 댓글 기록 조회 **/
    @GetMapping("/list-up/{member-id}")
    public BaseResponse<List<GetCommentRes>> getComments(@PathVariable(name = "member-id") Long memberId) {
        try{
            return new BaseResponse<>(commentService.getComments(memberId));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
