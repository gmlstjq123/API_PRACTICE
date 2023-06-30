package com.example.umc4_heron_template_jpa.member;

import com.example.umc4_heron_template_jpa.board.Board;
import com.example.umc4_heron_template_jpa.board.BoardRepository;
import com.example.umc4_heron_template_jpa.board.photo.dto.GetS3Res;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.login.dto.JwtResponseDTO;
import com.example.umc4_heron_template_jpa.login.jwt.JwtProvider;
import com.example.umc4_heron_template_jpa.member.dto.*;
import com.example.umc4_heron_template_jpa.member.profile.Profile;
import com.example.umc4_heron_template_jpa.member.profile.ProfileRepository;
import com.example.umc4_heron_template_jpa.member.profile.ProfileService;
import com.example.umc4_heron_template_jpa.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;

@EnableTransactionManagement
@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final ProfileRepository profileRepository;
    private final S3Service s3Service;
    private final JwtProvider jwtProvider;
    private final UtilService utilService;
    private final ProfileService profileService;
    // private final BCryptPasswordEncoder bCryptPasswordEncoder; // spring security login 사용 시 필요
    private final RedisTemplate redisTemplate;

    /**
     * 유저 생성 후 DB에 저장(회원 가입) with JWT
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
     * 유저 로그인 with JWT
     */
    public PostLoginRes login(PostLoginReq postLoginReq) throws BaseException {
        Member member = utilService.findByEmailWithValidation(postLoginReq.getEmail());
        String password; // DB에 저장된 암호화된 비밀번호를 복호화한 값을 저장하기 위한 변수
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

    /**
     * 유저 회원가입 with Spring Security
     */
//    @Transactional
//    public PostMemberRes securityCreateMember(PostMemberReq postMemberReq) throws BaseException {
//        if(memberRepository.findByEmailCount(postMemberReq.getEmail()) >= 1) {
//            throw new BaseException(POST_USERS_EXISTS_EMAIL);
//        }
//        if(postMemberReq.getPassword().isEmpty()){
//            throw new BaseException(PASSWORD_CANNOT_BE_NULL);
//        }
//        String pwd;
//        try{
//            // 암호화: postMemberReq에서 제공받은 비밀번호를 보안을 위해 암호화시켜 DB에 저장합니다.
//            pwd =  bCryptPasswordEncoder.encode(postMemberReq.getPassword());// 암호화코드
//        }
//        catch (Exception ignored) { // 암호화가 실패하였을 경우 에러 발생
//            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
//        }
//        try {
//            Member member = new Member();
//            member.createMember(postMemberReq.getEmail(), postMemberReq.getNickName(), pwd);
//            memberRepository.save(member);
//            return new PostMemberRes(member.getId(), member.getNickName());
//        } catch (Exception exception) { // DB에 이상이 있는 경우 에러 메시지를 보냅니다.
//            throw new BaseException(DATABASE_ERROR);
//        }
//    }

    /**
     * 유저 로그인 with Spring Security
     */
//    public PostLoginRes sercutiryLogin(PostLoginReq postLoginReq) throws BaseException {
//        Member member = utilService.findByEmailWithValidation(postLoginReq.getEmail());
//        String rawPassword = postLoginReq.getPassword(); // 입력 받은 비밀번호를 암호화한 값을 저장하기 위한 변수
//        if(bCryptPasswordEncoder.matches(rawPassword, member.getPassword())) {
//            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(member.getId());
//            String accessToken = tokenInfo.getAccessToken();
//            String refreshToken = tokenInfo.getRefreshToken();
//            member.updateAccessToken(accessToken);
//            member.updateRefreshToken(refreshToken);
//            memberRepository.save(member);
//            return new PostLoginRes(member.getId(), accessToken, refreshToken);
//        }
//        else{
//            throw new BaseException(FAILED_TO_LOGIN);
//        }
//    }

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
    public void modifyMemberName(PatchMemberReq patchMemberReq) {
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

    /**
     *  멤버 프로필 변경
     */
    @Transactional
    public String modifyProfile(Long memberId, MultipartFile multipartFile) throws BaseException {
        try {
            Member member = utilService.findByMemberIdWithValidation(memberId);
            Profile profile = profileRepository.findProfileById(memberId).orElse(null);
            if(profile == null) { // 프로필이 미등록된 사용자가 변경을 요청하는 경우
                GetS3Res getS3Res;
                if(multipartFile != null) {
                    getS3Res = s3Service.uploadSingleFile(multipartFile);
                    profileService.saveProfile(getS3Res, member);
                }
            }
            else { // 프로필이 등록된 사용자가 변경을 요청하는 경우
                    // 1. 버킷에서 삭제
                    profileService.deleteProfile(profile);
                    // 2. Profile Repository에서 삭제
                    profileService.deleteProfileById(memberId);
                    if(multipartFile != null) {
                        GetS3Res getS3Res = s3Service.uploadSingleFile(multipartFile);
                        profileService.saveProfile(getS3Res, member);
                    }
            }
            return "프로필 수정이 완료되었습니다.";
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }

    @Transactional
    public String logout(Member logoutMember) throws BaseException{
        try {
            String accessToken = logoutMember.getAccessToken();
            //엑세스 토큰 남은 유효시간
            Long expiration = jwtProvider.getExpiration(accessToken);
            //Redis Cache에 저장
            redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
            //리프레쉬 토큰 삭제
            logoutMember.updateRefreshToken("");
            memberRepository.save(logoutMember);
            String result = "로그아웃 되었습니다.";
            return result;
        } catch (Exception e) {
            throw new BaseException(FAILED_TO_LOGOUT);
        }

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
            String result = "로그아웃 되었습니다.";
            return result;
        }
        else {
            // 로그아웃 실패
            throw new BaseException(KAKAO_ERROR);
        }
    }
}


