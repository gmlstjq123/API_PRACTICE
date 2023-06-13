package com.example.umc4_heron_template_jpa.board;

import com.example.umc4_heron_template_jpa.board.dto.DeleteBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PatchBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PostBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PostBoardRes;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.config.BaseResponseStatus;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import com.example.umc4_heron_template_jpa.member.MemberService;
import com.example.umc4_heron_template_jpa.member.dto.DeleteMemberReq;
import com.example.umc4_heron_template_jpa.member.dto.PatchMemberReq;
import com.example.umc4_heron_template_jpa.utils.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;

@RestController
@RequiredArgsConstructor
public class BoardController {
    // 생성자 주입 방법을 통해 의존성 주입
    private final BoardService boardService;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final JwtService jwtService;
    /** 게시글 생성하기 **/
    @PostMapping("/board")
    public BaseResponse<PostBoardRes> createBoard(@RequestBody @Validated PostBoardReq postBoardReq) {
        try{
            return new BaseResponse<>(boardService.createBoard(postBoardReq));
        }
        catch(BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
    /** 게시글을 Id로 조회하기 **/
    @GetMapping("/board/{board-id}")
    public BaseResponse<PostBoardRes> getBoard(@PathVariable(name = "board-id") Long boardId) {
        if(boardId == 0){
            return new BaseResponse<>(INVALID_BOARD_ID);
        }
        try{
            Board board = boardService.getBoard(boardId);
            Member member = board.getMember();
            PostBoardRes postBoardRes = new PostBoardRes(member.getNickName(), board.getTitle(), board.getContent());
            return new BaseResponse<>(postBoardRes);
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }
    @DeleteMapping("/delete/{board-title}")
    public BaseResponse<String> deleteBoard(@PathVariable(name = "board-title") String title){
        try{
            boardService.deleteBoard(title);
            String result = "요청하신 게시글에 대한 삭제가 완료되었습니다.";
            return new BaseResponse<>(result);
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }
    @PatchMapping("/modify")
    public BaseResponse<String> modifyBoard(@RequestParam String email, @RequestParam String exTitle,
                                            @RequestParam String newTitle, @RequestParam String newContent) {
        try {
            Member member = memberRepository.findMemberByEmail(email);
            Long userIdxByJwt = jwtService.getUserIdx();
            if(!member.getId().equals(userIdxByJwt)){
                return new BaseResponse<>(INVALID_USER_JWT);
            }
            //같다면 게시글 내용을 변경
            List<Board> boards = boardRepository.findBoardByTitle(exTitle, member.getId());
            String result;
            if(boards.size()>1) {
                return new BaseResponse<>(SAME_TITLE_ERROR);
            }
            else if(boards.size()==1){
                PatchBoardReq patchBoardReq = new PatchBoardReq(member.getId(), newTitle, newContent);
                boardService.modifyBoard(patchBoardReq);
                result = "게시글 수정이 완료되었습니다.";

            }
            else {
                return new BaseResponse<>(NONE_EXIST_BOARD);
            }
            return new BaseResponse<>(result);
        }
        catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }
}