package com.hell.backend.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    @Operation(summary = "로그인 페이지", description = "로그인 페이지를 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "페이지 로드 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @Operation(summary = "회원가입 페이지", description = "회원가입 페이지를 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "페이지 로드 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @Operation(summary = "홈 페이지", description = "로그인 후 접근할 수 있는 홈 페이지를 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "홈 페이지 로드 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
