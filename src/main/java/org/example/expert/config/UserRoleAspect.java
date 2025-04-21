package org.example.expert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Slf4j
@Component
public class UserRoleAspect {

    @Around("execution(* org.example.expert.domain.user.service.UserAdminService.*(..))")
    public Object logBefore(ProceedingJoinPoint joinPoint) throws Throwable {


        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        ObjectMapper objectMapper = new ObjectMapper();

        UserRole userRole = UserRole.of(String.valueOf(request.getAttribute("userRole")));

        if (userRole != UserRole.ADMIN) {
            log.warn("권한 없음: {}", userRole);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 접근 가능합니다.");
            return null;
        }

        log.info("-AOP-");
        log.info("요청 URI: {}", request.getRequestURI());
        log.info("요청 사용자 ID: {}",  request.getAttribute("userId"));
        log.info("요청 사용자 역할: {}", request.getAttribute("userRole"));
        log.info("요청 메서드: {}", request.getMethod());
        log.info("요청 시각: {}", LocalDateTime.now());
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof UserRoleChangeRequest dto) {
                log.info("요청 DTO: {}", objectMapper.writeValueAsString(dto));
            }
        }

        Object result = joinPoint.proceed();

        log.info("응답 DTO: {}", objectMapper.writeValueAsString(result));

        return result;
    }
}
