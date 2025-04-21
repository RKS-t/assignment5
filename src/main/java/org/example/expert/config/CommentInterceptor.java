package org.example.expert.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Slf4j
@Component
public class CommentInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler
    ) throws Exception {

        String header = request.getHeader("Authorization");
        String token = jwtUtil.substringToken(header);

        Claims claims = jwtUtil.extractClaims(token);
        UserRole userRole = UserRole.of(claims.get("userRole", String.class));

        if (userRole != UserRole.ADMIN) {
            log.warn("권한 없음: {}", userRole);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 접근 가능합니다.");
            return false;
        }

        log.info("-Interceptor-");
        log.info("요청 URI: {}", request.getRequestURI());
        log.info("요청 사용자 ID: {}", claims.getSubject());
        log.info("요청 사용자 역할: {}", userRole);
        log.info("요청 메서드: {}", request.getMethod());
        log.info("요청 시각: {}", LocalDateTime.now());

        return true;
    }
}
