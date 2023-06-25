package com.example.umc4_heron_template_jpa.board.comment;

import com.example.umc4_heron_template_jpa.board.Board;
import com.example.umc4_heron_template_jpa.board.BoardRepository;
import com.example.umc4_heron_template_jpa.board.comment.dto.GetCommentRes;
import com.example.umc4_heron_template_jpa.board.comment.dto.PostCommentReq;
import com.example.umc4_heron_template_jpa.board.comment.dto.PostCommentRes;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import com.example.umc4_heron_template_jpa.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UtilService utilService;
    public PostCommentRes addComment(Long memberId, PostCommentReq postCommentReq) throws BaseException {
        Member member = utilService.findByMemberIdWithValidation(memberId);
        Board board = utilService.findByBoardIdWithValidation(postCommentReq.getBoardId());
        Comment comment;
        comment = Comment.builder()
                .nickName(member.getNickName())
                .reply(postCommentReq.getReply())
                .member(member)
                .board(board)
                .build();
        commentRepository.save(comment);
        return new PostCommentRes(member.getNickName(), postCommentReq.getReply());
    }

    /** 같은 게시글에 작성한 댓글이 따로 표시되어 불편 **/
/*  public List<GetCommentRes> getComments(Long memberId) throws BaseException {
        try{
            List<Comment> comments = commentRepository.findCommentsByMemberId(memberId);
            List<GetCommentRes> getCommentRes = comments.stream()
                    .map(comment -> new GetCommentRes(comment.getBoard().getTitle(), comment.getReply()))
                    .collect(Collectors.toList());
            return getCommentRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    } */

    /** 같은 게시글에 작성한 댓글은 한번에 모아서 출력 **/
    public List<GetCommentRes> getComments(Long memberId) throws BaseException {
        try {
            Member member = utilService.findByMemberIdWithValidation(memberId);
            List<Comment> comments = commentRepository.findCommentsByMemberId(memberId);
            Map<String, List<Comment>> commentsByTitle = comments.stream()
                    .collect(Collectors.groupingBy(comment -> comment.getBoard().getTitle()));

            List<GetCommentRes> getCommentRes = new ArrayList<>();
            for (Map.Entry<String, List<Comment>> entry : commentsByTitle.entrySet()) {
                String title = entry.getKey();
                List<String> replies = entry.getValue().stream()
                        .map(Comment::getReply)
                        .collect(Collectors.toList());

                getCommentRes.add(new GetCommentRes(title, replies));
            }

            return getCommentRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }
}
