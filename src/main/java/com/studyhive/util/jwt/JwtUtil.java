package com.studyhive.util.jwt;

import com.studyhive.model.entity.User;
import com.studyhive.repository.interfaces.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final UserRepository userRepository;
    private final String secretKey;
    private final Key key;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours


    public JwtUtil(UserRepository userRepository, @Value("${SECRET_KEY}") String secretKey) {
        this.userRepository = userRepository;
        this.secretKey = secretKey;
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(UUID userId) {

//        User user = userRepository.getByUserId(userId);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID getUserId(String token) {

        return UUID.fromString(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject()
        );
    }

    public boolean isTokenValid(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Date getExpirationDate(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    public Date getIssueDate(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getIssuedAt();
    }
}
