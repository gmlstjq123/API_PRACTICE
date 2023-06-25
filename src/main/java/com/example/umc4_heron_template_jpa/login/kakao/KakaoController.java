package com.example.umc4_heron_template_jpa.login.kakao;

import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.config.BaseResponseStatus;
import com.example.umc4_heron_template_jpa.login.dto.JwtResponseDTO;
import com.example.umc4_heron_template_jpa.login.jwt.JwtProvider;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import com.example.umc4_heron_template_jpa.member.MemberService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kaKaoLoginService;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    //카카오 로그인 코드
    @ResponseBody
    @PostMapping("/oauth/kakao")
    public BaseResponse<?> kakaoCallback(@RequestParam("accToken") String accessToken,
                                         @RequestParam("refToken") String refreshToken) {
        String memberEmail = kaKaoLoginService.getMemberEmail(accessToken);
        String memberNickName = kaKaoLoginService.getMemberNickname(accessToken);
        Optional<Member> findMember = memberRepository.findByEmail(memberEmail);
        if (!findMember.isPresent()) {
            Member kakaoMember = new Member();
            kakaoMember.updateEmail(memberEmail);
            kakaoMember.updateNickName(memberNickName);
            kakaoMember.updateIsSocialLogin();
            JwtResponseDTO.TokenInfo tokenInfo = JwtResponseDTO.TokenInfo.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            kakaoMember.updateAccessToken(accessToken);
            kakaoMember.updateRefreshToken(refreshToken);
            memberRepository.save(kakaoMember);
            return new BaseResponse<>(tokenInfo);
        }

        else {
            Member member = findMember.get();
            JwtResponseDTO.TokenInfo tokenInfo = jwtProvider.generateToken(member.getId());
            member.updateRefreshToken(tokenInfo.getRefreshToken());
            member.updateIsSocialLogin();
            memberRepository.save(member);
            return new BaseResponse<>(tokenInfo);
        }
    }
}

