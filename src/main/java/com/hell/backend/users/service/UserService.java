package com.hell.backend.users.service;

import com.hell.backend.security.JwtTokenProvider;
import com.hell.backend.users.dto.LoginRequest;
import com.hell.backend.users.dto.SignUpRequest;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public void registerUser(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 비밀번호 암호화
        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty() || !passwordEncoder.matches(request.getPassword(), optionalUser.get().getPassword())) {
            throw new IllegalArgumentException("잘못된 이메일 또는 비밀번호");
        }
        return jwtTokenProvider.generateToken(optionalUser.get().getEmail());
    }
}
