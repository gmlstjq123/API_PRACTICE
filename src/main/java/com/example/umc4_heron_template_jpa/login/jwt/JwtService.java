package com.example.umc4_heron_template_jpa.login.jwt;

import com.example.umc4_heron_template_jpa.board.dto.DeleteBoardReq;
import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponse;
import com.example.umc4_heron_template_jpa.member.Member;
import com.example.umc4_heron_template_jpa.member.MemberRepository;
import com.example.umc4_heron_template_jpa.utils.Secret;
import com.example.umc4_heron_template_jpa.utils.UtilService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jdk.jshell.execution.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Date;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class JwtService {
    private Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(Secret.JWT_SECRET_KEY));
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    /**
     * Header에서 Authorization 으로 JWT 추출
     */
    public String getJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("Authorization");
    }

    public String getRefJwt(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("AuthorizationRef");
    }
    /*
    JWT에서 userId 추출
     */
    public Long getMemberIdx() throws BaseException {
        // 1. JWT 추출
        String accessToken = getJwt();
        if (accessToken == null || accessToken.length() == 0) {
            throw new BaseException(EMPTY_JWT);
        }

        try {
            // 2. JWT parsing
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);

            // 3. userId 추출
            return claims.getBody().get("memberId", Long.class);
        } catch (ExpiredJwtException e) {
            // access token이 만료된 경우
            Member member = memberRepository.findMemberByAccessToken(accessToken).orElse(null);
            if (member == null) {
                throw new BaseException(EXPIRED_USER_JWT);
            }

            // 4. Refresh Token을 사용하여 새로운 Access Token 발급
            String refreshToken = member.getRefreshToken();
            if (refreshToken != null) {
                String newAccessToken = refreshAccessToken(member, refreshToken);
                // 새로운 Access Token으로 업데이트된 JWT를 사용하여 userId 추출
                Jws<Claims> newClaims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(newAccessToken);
                return newClaims.getBody().get("memberId", Long.class);
            } else {
                throw new BaseException(EXPIRED_USER_JWT);
            }
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new BaseException(INVALID_JWT);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }
    }

    public String refreshAccessToken(Member member, String refreshToken) throws BaseException {
        try {
            // 리프레시 토큰도 만료된 경우
            if (jwtProvider.getExpiration(refreshToken) <= 0) {
                throw new BaseException(EXPIRED_USER_JWT);
            } else { // 리프레시 토큰은 만료되지 않은 경우
                if (!jwtProvider.validateToken(refreshToken)) {
                    throw new BaseException(INVALID_JWT);
                }
                Long memberId = member.getId();
                String refreshedAccessToken = jwtProvider.createToken(memberId);
                // 액세스 토큰 재발급에 성공한 경우
                if (refreshedAccessToken != null) {
                    member.updateAccessToken(refreshedAccessToken);
                    memberRepository.save(member);
                    return refreshedAccessToken;
                }
                throw new BaseException(FAILED_TO_REFRESH);
            }
        } catch (BaseException exception) {
            throw new BaseException(exception.getStatus());
        }
    }
}

