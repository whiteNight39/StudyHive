package com.studyhive.util.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhive.model.response.BaseResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        String jwtToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
        }

        if (jwtToken != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                if (jwtUtil.isTokenValid(jwtToken)) {
                    UUID userId = jwtUtil.getUserId(jwtToken);

                    CustomUserPrincipal principal = new CustomUserPrincipal(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, List.of());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException ex) {
                logger.warn("JWT expired at: " + ex.getClaims().getExpiration());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                BaseResponse<?> error = new BaseResponse<>("33", "Authentication failed", "JWT expired at: " + ex.getClaims().getExpiration());
                response.getWriter().write(new ObjectMapper().writeValueAsString(error));
                return;
            } catch (Exception e) {
                logger.warn("JWT error: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                BaseResponse<?> error = new BaseResponse<>("33", "Authentication failed", "Invalid JWT: " + e.getMessage());
                response.getWriter().write(new ObjectMapper().writeValueAsString(error));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
