package com.hell.backend.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {

    @Schema(description = "사용자의 이메일", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "사용자의 비밀번호", example = "password123", required = true)
    private String password;

}
