package com.example.umc4_heron_template_jpa.member;

import com.example.umc4_heron_template_jpa.board.Board;
import com.example.umc4_heron_template_jpa.board.BoardRepository;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.login.dto.JwtResponseDTO;
import com.example.umc4_heron_template_jpa.login.jwt.JwtProvider;
import com.example.umc4_heron_template_jpa.login.jwt.JwtService;
import com.example.umc4_heron_template_jpa.member.dto.*;
import com.example.umc4_heron_template_jpa.utils.AES128;
import com.example.umc4_heron_template_jpa.utils.Secret;
import com.example.umc4_heron_template_jpa.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;

@EnableTransactionManagement
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final JwtService jwtService;
    private final JwtProvider jwtProvider;
    private final UtilService utilService;

    /**
     * 유저 생성 후 DB에 저장
     */
    @Transactional
    public PostMemberRes createMember(PostMemberReq postMemberReq) throws BaseException {
        if(memberRepository.findByEmailCount(postMemberReq.getEmail()) >= 1) {
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }
        if(postMemberReq.getPassword().isEmpty()){
            throw new BaseException(PASSWORD_CANNOT_BE_NULL);
        }
        String pwd;
        try{
            // 암호화: postUserReq에서 제공받은 비밀번호를 보안을 위해 암호화시켜 DB에 저장합니다.
            // ex) password123 -> dfhsjfkjdsnj4@!$!@chdsnjfwkenjfnsjfnjsd.fdsfaifsadjfjaf
            pwd = new AES128(Secret.USER_INFO_PASSWORD_KEY).encrypt(postMemberReq.getPassword()); // 암호화코드
        }
        catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }
        try {
            Member member = new Member();
            member.createMember(postMemberReq.getEmail(), postMemberReq.getNickName(), pwd);
            memberRepository.save(member);
            return new PostMemberRes(member.getId(), member.getNickName());
        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 유저 로그인
     */
    public PostLoginRes login(PostLoginReq postLoginReq) throws BaseException {
        Member member = utilService.findByMemberIdWithValidation(postLoginReq.getMemberId());
        String password;
        try{
            password = new AES128(Secret.USER_INFO_PASSWORD_KEY).decrypt(member.getPassword()); // 복호화
        } catch (Exception ignored) {
            throw new BaseException(PASSWORD_DECRYPTION_ERROR);
        }
        if(postLoginReq.getPassword().equals(password)){
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(member.getId());
            String accessToken = tokenInfo.getAccessToken();
            String refreshToken = tokenInfo.getRefreshToken();
            member.updateAccessToken(accessToken);
            member.updateRefreshToken(refreshToken);
            memberRepository.save(member);
            return new PostLoginRes(member.getId(), accessToken, refreshToken);
        }
        else{
            throw new BaseException(FAILED_TO_LOGIN);
        }
    }

    public String logout(String accessToken) throws BaseException {
        try{
            if (accessToken == null || accessToken.isEmpty()) {
                throw new BaseException(INVALID_JWT);
            }
            Member member = utilService.findByAccessTokenWithValidation(accessToken);
            if(!jwtProvider.validateToken(accessToken)) {
                throw new BaseException(INVALID_JWT);
            }
            member.updateRefreshToken("");
            member.updateAccessToken("");
            memberRepository.save(member);
            String result = "로그아웃 되었습니다";
            return result;
        } catch(Exception ignored) {
            throw new BaseException(FAILED_TO_LOGOUT);
        }
    }
    /**
     * 모든 회원 조회
     */
    public List<GetMemberRes> getMembers() throws BaseException {
        try{
            List<Member> members = memberRepository.findMembers(); //Member List로 받아 GetMemberRes로 바꿔줌
            List<GetMemberRes> getMemberRes = members.stream()
                    .map(member -> new GetMemberRes(member.getId(), member.getNickName(), member.getEmail(), member.getPassword()))
                    .collect(Collectors.toList());
            return getMemberRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 특정 닉네임 조회
     */
    public List<GetMemberRes> getMembersByNickname(String nickname) throws BaseException {
        try{
            List<Member> members = memberRepository.findMemberByNickName(nickname);
            List<GetMemberRes> GetMemberRes = members.stream()
                    .map(member -> new GetMemberRes(member.getId(), member.getNickName(), member.getEmail(), member.getPassword()))
                    .collect(Collectors.toList());
            return GetMemberRes;
        } catch (Exception exception) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    /**
     * 닉네임 변경
     */
    @Transactional
    public void modifyUserName(PatchMemberReq patchMemberReq) {
        Member member = memberRepository.getReferenceById(patchMemberReq.getMemberId());
        member.updateNickName(patchMemberReq.getNickName());
    }

    @Transactional
    public String deleteMember(Long memberId) throws BaseException{
        Member member = utilService.findByMemberIdWithValidation(memberId);
        List<Board> boards = boardRepository.findBoardByMemberId(member.getId());
        if(!boards.isEmpty()){
            throw new BaseException(CANNOT_DELETE);
        }
        memberRepository.deleteMember(member.getEmail());
        String result = "요청하신 회원에 대한 삭제가 완료되었습니다.";
        return result;
    }

    //액세스 토큰, 리프레시 토큰 함께 재발급
    public JwtResponseDTO.TokenInfo reissue(Long memberId) {
        Member member = memberRepository.findById(memberId).get();
        JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(memberId);
        member.updateRefreshToken(tokenInfo.getRefreshToken());
        memberRepository.save(member);
        return tokenInfo;
    }

    public String socialLogout(String accessToken) throws BaseException{
        // HttpHeader 생성
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);

        // HttpHeader를 포함한 요청 객체 생성
        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        // RestTemplate를 이용하여 로그아웃 요청 보내기
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://kapi.kakao.com/v1/user/unlink",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        // 응답 확인
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            // 로그아웃 성공
            String result = "로그아웃되었습니다.";
            Member member = utilService.findByAccessTokenWithValidation(accessToken);
            member.updateAccessToken("");
            member.updateRefreshToken("");
            memberRepository.save(member);
            return result;
        }
        else {
            // 로그아웃 실패
            throw new BaseException(FAILED_TO_LOGOUT);
        }
    }
}


