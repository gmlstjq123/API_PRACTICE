package com.example.umc4_heron_template_jpa.member;

import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.login.jwt.JwtProvider;
import com.example.umc4_heron_template_jpa.login.jwt.JwtService;
import com.example.umc4_heron_template_jpa.login.kakao.KakaoService;
import com.example.umc4_heron_template_jpa.member.dto.*;
import com.example.umc4_heron_template_jpa.member.profile.ProfileService;
import com.example.umc4_heron_template_jpa.member.profile.dto.PostProfileReq;
import com.example.umc4_heron_template_jpa.utils.UtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;
import static com.example.umc4_heron_template_jpa.utils.ValidationRegex.isRegexEmail;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final ProfileService profileService;
    private final JwtProvider jwtProvider;
    private final UtilService utilService;
    /**
     * 회원 가입
     */
    @PostMapping("/create")
    public BaseResponse<PostMemberRes> createMember(@RequestBody PostMemberReq postMemberReq){
        if(!isRegexEmail(postMemberReq.getEmail())) return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
        try{
            return new BaseResponse<>(memberService.createMember(postMemberReq));
        } catch (BaseException exception) {
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/log-in")
    public BaseResponse<PostLoginRes> loginMember(@RequestBody PostLoginReq postLoginReq){
        try{
            if(!isRegexEmail(postLoginReq.getEmail())) return new BaseResponse<>(POST_USERS_INVALID_EMAIL);
            return new BaseResponse<>(memberService.login(postLoginReq));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @PostMapping("/log-out")
    public BaseResponse<String> logoutMember() {
        try {
            Long memberId = jwtService.getMemberIdx();
            Member logoutMember = utilService.findByMemberIdWithValidation(memberId);
            String accessToken = logoutMember.getAccessToken();
            String result = memberService.logout(memberId, accessToken);
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 회원 조회
     * nickname이 파라미터에 없을 경우 모두 조회
     */
    @GetMapping("Read")
    public BaseResponse<List<GetMemberRes>> getMembers(@RequestParam(required = false) String nickName){
        //  @RequestParam은, 1개의 HTTP Request 파라미터를 받을 수 있는 어노테이션(?뒤의 값).
        //  default로 RequestParam은 반드시 값이 존재해야 하도록 설정되어 있지만, (전송 안되면 400 Error 유발)
        //  지금 예시와 같이 required 설정으로 필수 값에서 제외 시킬 수 있음
        //  defaultValue를 통해, 기본값(파라미터가 없는 경우, 해당 파라미터의 기본값 설정)을 지정할 수 있음
        try{
            if (nickName == null) { // query string인 nickname이 없을 경우, 그냥 전체 유저정보를 불러온다.
                return new BaseResponse<>(memberService.getMembers());
            }
            // query string인 nickname이 있을 경우, 조건을 만족하는 유저정보들을 불러온다.
            return new BaseResponse<>(memberService.getMembersByNickname(nickName));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }


    /**
     * 멤버 닉네임 변경
     */
    @PatchMapping("/update")
    public BaseResponse<String> modifyMemberName(@RequestParam String nickName) {
        // PostMan에서 Headers에 Authorization필드를 추가하고, 로그인할 때 받은 jwt 토큰을 입력해야 실행이 됩니다.
        try {
            Long memberId = jwtService.getMemberIdx();
            Member member = utilService.findByMemberIdWithValidation(memberId);
            PatchMemberReq patchMemberReq = new PatchMemberReq(member.getId(), nickName);
            memberService.modifyMemberName(patchMemberReq);
            String result = "회원정보가 수정되었습니다.";
            return new BaseResponse<>(result);
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    /**
     * 멤버 프로필 변경
     */
    @PatchMapping("/update-profile")
    public BaseResponse<String> modifyMemberProfile(@RequestPart(value = "image", required = false) MultipartFile multipartFile) {
        try {
            Long memberId = jwtService.getMemberIdx();
            return new BaseResponse<>(memberService.modifyProfile(memberId, multipartFile));
        } catch (BaseException exception) {
            return new BaseResponse<>(exception.getStatus());
        }
    }

    @DeleteMapping("/delete")
    public BaseResponse<String> deleteMember(){
        try{
            Long memberId = jwtService.getMemberIdx();
            return new BaseResponse<>(memberService.deleteMember(memberId));
        } catch(BaseException exception){
            return new BaseResponse<>(exception.getStatus());
        }
    }
}
