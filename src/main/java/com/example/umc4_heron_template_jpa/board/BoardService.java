package com.example.umc4_heron_template_jpa.board;

import com.example.umc4_heron_template_jpa.board.dto.DeleteBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PatchBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PostBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PostBoardRes;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import com.example.umc4_heron_template_jpa.member.dto.DeleteMemberReq;
import com.example.umc4_heron_template_jpa.member.dto.PatchMemberReq;
import com.example.umc4_heron_template_jpa.member.dto.PostMemberRes;
import com.example.umc4_heron_template_jpa.utils.AES128;
import com.example.umc4_heron_template_jpa.utils.Secret;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;
@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    @Transactional
    public void save(Board board){
        boardRepository.save(board);
    }
    @Transactional
    public PostBoardRes createBoard(PostBoardReq postBoardReq) throws BaseException {
        Member member = memberRepository.findMemberByEmail(postBoardReq.getEmail());
        if (member == null) {
            throw new BaseException(POST_USERS_NONE_EXISTS_EMAIL);
        }
        Board board;
        board = Board.builder()
                .title(postBoardReq.getTitle())
                .content(postBoardReq.getContent())
                .member(member)
                .build();
        save(board);
        return new PostBoardRes(member.getNickName(), board.getTitle(), board.getContent());
    }

    @Transactional
    public Board getBoard(Long boardID) throws BaseException{
        Board board = boardRepository.findBoardById(boardID);
        if(board == null){
            throw new BaseException(NONE_EXIST_BOARD);
        }
        return board;
    }

    @Transactional
    public void deleteBoard(String title) throws BaseException{
        List<Board> deleteBoards = boardRepository.findBoardByTitle(title);
        if(deleteBoards.size()==0){
            throw new BaseException(BOARD_NOT_FOUND);
        }
        for (Board deleteBoard : deleteBoards) {
            boardRepository.deleteBoard(deleteBoard.getTitle());
        }
    }

    @Transactional
    public void modifyBoard(PatchBoardReq patchBoardReq) {
        Board board = boardRepository.getReferenceById(patchBoardReq.getBoardId());
        board.updateBoard(patchBoardReq.getTitle(), patchBoardReq.getContent());
    }
}
