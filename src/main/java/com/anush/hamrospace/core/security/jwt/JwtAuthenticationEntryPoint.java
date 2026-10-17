package com.anush.hamrospace.core.security.jwt;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.anush.hamrospace.common.dto.response.ApiResponse;
import com.anush.hamrospace.core.exception.ErrorCodes;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Boolean jwtFailure = (Boolean) request.getAttribute("jwt-auth-failure");

        if (Boolean.TRUE.equals(jwtFailure)) {
            objectMapper.writeValue(
                    response.getWriter(),
                    ApiResponse.fail("Invalid or expired token", ErrorCodes.INVALID_OR_EXPIRED_TOKEN));
        } else {
            objectMapper.writeValue(
                    response.getWriter(),
                    ApiResponse.fail("Authentication required", ErrorCodes.AUTH_REQUIRED));
        }
    }

}
