package com.example.umc4_heron_template_jpa.login.jwt;

import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.config.BaseResponseStatus;
import com.example.umc4_heron_template_jpa.login.dto.JwtResponseDTO;
import com.example.umc4_heron_template_jpa.utils.Secret;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.FAILED_TO_UPDATE;
import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.INVALID_JWT;

@Slf4j
@Component
public class JwtProvider {
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 14 * 24 * 60 * 60 * 1000L; //refreshToken 유효기간 14일
    // private static final long ACCESS_TOKEN_EXPIRE_TIME = 1 * 6 * 60 * 60 * 1000L; //accessToken 유효기간 6시간
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 10 * 1000L; //유효기간 1분, refrshToken 테스트를 위해 사용
    private static final String BEARER_TYPE = "Bearer";

    private Key key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(Secret.JWT_SECRET_KEY));

    //==토큰 생성 메소드==//
    public String createToken(Long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME); // 만료기간 6시간

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("memberId", memberId)
                .setIssuer("test") // 토큰발급자(iss)
                .setIssuedAt(now) // 발급시간(iat)
                .setExpiration(expiration) // 만료시간(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME); // 만료기간 14일

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("memberId", memberId)
                .setIssuer("test") // 토큰발급자(iss)
                .setIssuedAt(now) // 발급시간(iat)
                .setExpiration(expiration) // 만료시간(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 유저 정보를 가지고 AccessToken, RefreshToken 을 생성하는 메서드
    public JwtResponseDTO.TokenInfo generateToken(Long memberId) {

        long now = (new Date()).getTime();
        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("memberId", memberId)
                .setExpiration(accessTokenExpiresIn) // 만료시간(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // (1)
                .claim("memberId", memberId)
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return JwtResponseDTO.TokenInfo.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Long getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        String memberId = claims.get("memberId").toString();

        return Long.valueOf(memberId);

    }

    //==Jwt 토큰의 유효성 체크 메소드==//
    public Claims parseJwtToken(String token) {
        token = BearerRemove(token); // Bearer 제거
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public boolean validateTokenWithoutExpiration(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    //==토큰 앞 부분('Bearer') 제거 메소드==//
    private String BearerRemove(String token) {
        return token.substring("Bearer ".length());
    }

    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody().getExpiration();
        // 현재 시간
        Long now = System.currentTimeMillis();
        return (expiration.getTime() - now);
    }

    /** 토큰을 의도적으로 만료시키는 메서드 **/
    public String makeInvalidToken(String token) throws BaseException {
        if(validateTokenWithoutExpiration(token)){ // 토큰 만료는 제외하고 토큰의 유효성을 평가
            if(getExpiration(token) <= 0) { // 이미 만료된 토큰이라면
                return token; // 굳이 다른 처리해줄 필요 없이 바로 리턴
            }
            String updatedToken = updateExpirationCurrentTime(token);
            return updatedToken;
        }
        else {
            throw new BaseException(INVALID_JWT);
        }
    }

    /** 만료 시간을 현재 시간으로 업데이트 하는 메서드 **/
    private String updateExpirationCurrentTime(String token) throws BaseException{
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody();
            claims.setExpiration(Date.from(Instant.now()));

            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(SignatureAlgorithm.HS256, key)
                    .compact();
        } catch (Exception e) {
            throw new BaseException(FAILED_TO_UPDATE);
        }
    }
}