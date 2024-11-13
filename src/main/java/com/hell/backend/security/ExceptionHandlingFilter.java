package com.hell.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ExceptionHandlingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch ( DataIntegrityViolationException ex) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.getWriter().write("이미 존재하는 이메일입니다.");
        } catch (Exception ex) {
            // 다른 예외는 그대로 던집니다.
            throw ex;
        }
    }
}
