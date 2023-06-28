package com.example.umc4_heron_template_jpa.board;

import com.example.umc4_heron_template_jpa.board.dto.DeleteBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PatchBoardReq;
import com.example.umc4_heron_template_jpa.board.dto.PostBoardReq;
import com.example.umc4_heron_template_jpa.board.photo.PostPhoto;
import com.example.umc4_heron_template_jpa.board.photo.PostPhotoRepository;
import com.example.umc4_heron_template_jpa.board.photo.PostPhotoService;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.config.BaseResponseStatus;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import com.example.umc4_heron_template_jpa.utils.S3Service;
import com.example.umc4_heron_template_jpa.utils.UtilService;
import com.example.umc4_heron_template_jpa.board.photo.dto.GetS3Res;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final PostPhotoRepository postPhotoRepository;
    private final UtilService utilService;
    private final S3Service s3Service;
    private final PostPhotoService postPhotoService;
    @Transactional
    public void save(Board board){
        boardRepository.save(board);
    }

    @Transactional
    public String createBoard(Long memberId, PostBoardReq postBoardReq, List<MultipartFile> multipartFiles) throws BaseException {
        Member member = utilService.findByMemberIdWithValidation(memberId);
        Board board = Board.builder()
                .title(postBoardReq.getTitle())
                .content(postBoardReq.getContent())
                .photoList(new ArrayList<>())
                .member(member)
                .commentList(new ArrayList<>())
                .build();
        save(board);

        if(multipartFiles != null) {
            List<GetS3Res> getS3ResList = s3Service.uploadFile(multipartFiles);
            postPhotoService.saveAllPostPhotoByBoard(getS3ResList, board);
        }

        return "boardId: " + board.getBoardId() + "인 게시글을 생성했습니다.";
    }

    @Transactional
    public Board getBoard(Long boardId) throws BaseException{
        Board board = utilService.findByBoardIdWithValidation(boardId);
        return board;
    }

    @Transactional
    public String deleteBoard(DeleteBoardReq deleteBoardReq) throws BaseException {
        Board deleteBoard = utilService.findByBoardIdWithValidation(deleteBoardReq.getBoardId());
        Member writer = deleteBoard.getMember();
        Member visitor = utilService.findByMemberIdWithValidation(deleteBoardReq.getMemberId());
        if(writer.getId() == visitor.getId()) {
            // S3에 업로드된 파일을 삭제하는 명령
            List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(deleteBoard.getBoardId());
            postPhotoService.deleteAllPostPhotos(allByBoardId);
            // PostPhotoRepository에서 삭제하는 명령
            List<Long> ids = postPhotoService.findAllId(deleteBoard.getBoardId());
            postPhotoService.deleteAllPostPhotoByBoard(ids);
            // 아래의 JPQL 쿼리로 한 번에 PostPhoto들을 삭제하는 것도 가능.
            // postPhotoRepository.deletePostPhotoByBoardId(deleteBoardReq.getBoardId());

            // 게시글을 삭제하는 명령
            boardRepository.deleteBoard(deleteBoard.getBoardId());
            String result = "요청하신 게시글에 대한 삭제가 완료되었습니다.";
            return result;
        }
        else {
           throw new BaseException(BaseResponseStatus.MEMBER_WITHOUT_PERMISSION);
        }
    }

    @Transactional
    public String modifyBoard(Long memberId, Long boardId, PatchBoardReq patchBoardReq,
                              List<MultipartFile> multipartFiles) throws BaseException {
        try {
            Board board = utilService.findByBoardIdWithValidation(boardId);
            Member writer = board.getMember();
            Member visitor = utilService.findByMemberIdWithValidation(memberId);
            if(writer.getId() == visitor.getId()){
                board.updateBoard(patchBoardReq.getTitle(), patchBoardReq.getContent());
                //사진 업데이트, 지우고 다시 저장!
                List<PostPhoto> allByBoardId = postPhotoService.findAllByBoardId(boardId);
                postPhotoService.deleteAllPostPhotos(allByBoardId);
                List<Long> ids = postPhotoService.findAllId(board.getBoardId());
                postPhotoService.deleteAllPostPhotoByBoard(ids);

                if(multipartFiles != null) {
                    List<GetS3Res> getS3ResList = s3Service.uploadFile(multipartFiles);
                    postPhotoService.saveAllPostPhotoByBoard(getS3ResList, board);
                }

                return "boardId " + board.getBoardId() + "의 게시글을 수정했습니다.";
            }
            else {
                throw new BaseException(BaseResponseStatus.MEMBER_WITHOUT_PERMISSION);
            }
        } catch(BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }
}
