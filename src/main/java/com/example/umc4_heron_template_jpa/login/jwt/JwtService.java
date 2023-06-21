package com.example.umc4_heron_template_jpa.login.jwt;

import com.example.umc4_heron_template_jpa.config.BaseException;
import com.example.umc4_heron_template_jpa.utils.Secret;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

import static com.example.umc4_heron_template_jpa.config.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class JwtService {
    private Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(Secret.JWT_SECRET_KEY));

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

        //1. JWT 추출
        String accessToken = getJwt();
        if(accessToken == null || accessToken.length() == 0){
            throw new BaseException(EMPTY_JWT);
        }

        // 2. JWT parsing
        Jws<Claims> claims;
        try{
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new BaseException(INVALID_JWT);
        } catch (ExpiredJwtException e) {
            throw new BaseException(EXPIRED_USER_JWT);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }

        // 3. userId 추출
        return claims.getBody().get("memberId",Long.class);
    }

    public Long getMemberIdxWithToken(String accessToken) throws BaseException {
        // JWT parsing
        Jws<Claims> claims;
        try{
            claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new BaseException(INVALID_JWT);
        } catch (ExpiredJwtException e) {
            throw new BaseException(EXPIRED_USER_JWT);
        } catch (Exception ignored) {
            throw new BaseException(INVALID_JWT);
        }

        // 3. userId 추출
        return claims.getBody().get("memberId",Long.class);
    }
}

