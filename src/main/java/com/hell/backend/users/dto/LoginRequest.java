package com.hell.backend.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    @Schema(description = "사용자 이메일 주소", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "사용자 이메일 주소", example = "password123", required = true)
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;
}
