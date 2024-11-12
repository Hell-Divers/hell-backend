package com.hell.backend.users.service;

import com.hell.backend.security.JwtTokenProvider;
import com.hell.backend.users.dto.LoginRequest;
import com.hell.backend.users.dto.SignUpRequest;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
<<<<<<< HEAD
=======
    public Long save(SignUpRequest dto) {
        return userRepository.save(User.builder()
                .email(dto.getEmail())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
//                .nickname(dto.getNickname())
                .build()).getId();
    }

    // 중복 이메일일 경우 예외 처리
>>>>>>> 5ffb5f7 (fix: remove nickname field)
    public void registerUser(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("Email already in use");
        }

        // nickname이 null일 경우 기본값 설정
        String nickname = request.getNickname() != null ? request.getNickname() : "DefaultNickname";

        User user = User.builder()
                .email(request.getEmail())
                .password(bCryptPasswordEncoder.encode(request.getPassword()))
<<<<<<< HEAD
                .nickname(nickname)
=======
//                .nickname(request.getNickname())
>>>>>>> 5ffb5f7 (fix: remove nickname field)
                .build();

        userRepository.save(user);
    }

    // 로그인 로직
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return jwtTokenProvider.generateToken(user.getEmail());
    }
}
