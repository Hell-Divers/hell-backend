package com.hell.backend.users.controller;

import com.hell.backend.users.dto.LoginRequest;
import com.hell.backend.users.dto.SignUpRequest;
import com.hell.backend.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "회원 가입", description = "새로운 사용자를 등록하는 API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원 가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @Operation(summary = "사용자 로그인", description = "사용자 로그인을 수행하고 JWT 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "필수 필드 누락"),
            @ApiResponse(responseCode = "401", description = "잘못된 이메일 또는 비밀번호"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 필드가 누락되었습니다.");
        }

        String token = userService.login(request);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new ResponseEntity<>("Login successful", headers, HttpStatus.OK);
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
