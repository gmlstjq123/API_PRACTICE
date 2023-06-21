package com.example.umc4_heron_template_jpa.login.jwt;

import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.member.MemberService;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwtController {
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    private final MemberService memberService;

    //==토큰 생성 컨트롤러==//

    public String createToken(Long memberId) throws Exception {
        String token = jwtProvider.createToken(memberId); // 토큰 생성
        Claims claims = jwtProvider.parseJwtToken("Bearer "+ token); // 토큰 검증

        TokenDataResponse tokenDataResponse = new TokenDataResponse(token, claims.getSubject(), claims.getIssuedAt().toString(), claims.getExpiration().toString());
        TokenResponse tokenResponse = new TokenResponse("200", "OK", tokenDataResponse);

        return tokenDataResponse.token;
    }

    public TokenDataResponse createRefreshToken(Long memberId) throws Exception {
        String token = jwtProvider.createRefreshToken(memberId); // 토큰 생성
        Claims claims = jwtProvider.parseJwtToken("Bearer "+ token); // 토큰 검증

        TokenDataResponse tokenDataResponse = new TokenDataResponse(token, claims.getSubject(), claims.getIssuedAt().toString(),  claims.getExpiration().toString());
        TokenResponse tokenResponse = new TokenResponse("200", "OK", tokenDataResponse);

        return tokenDataResponse;
    }

    //==토큰 인증 컨트롤러==//
    @GetMapping(value = "/checkToken")
    public TokenResponseNoData checkToken(@RequestHeader(value = "Authorization") String token) throws Exception {
        Claims claims = jwtProvider.parseJwtToken(token);

        TokenResponseNoData tokenResponseNoData = new TokenResponseNoData("200", "success");
        return tokenResponseNoData;
    }



    //==Response DTO==//
    @Data
    @AllArgsConstructor
    static class TokenResponse<T> {

        private String code;
        private String msg;
        private T data;
    }

    //==Response DTO==//
    @Data
    @AllArgsConstructor
    static class TokenResponseNoData<T> {

        private String code;
        private String msg;
    }

    //==Response DTO==//
    @Data
    @AllArgsConstructor
    static class TokenDataResponse {
        private String token;
        private String subject;
        private String issued_time;
        private String expired_time;
    }
}
