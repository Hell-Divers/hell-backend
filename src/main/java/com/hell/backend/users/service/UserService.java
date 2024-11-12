package com.hell.backend.users.service;

import com.hell.backend.security.JwtTokenProvider;
import com.hell.backend.users.dto.LoginRequest;
import com.hell.backend.users.dto.SignUpRequest;
import com.hell.backend.users.entity.User;
import com.hell.backend.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mysql.cj.conf.PropertyKey.logger;
@Slf4j
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    //request log로 출력해보기
    public void registerUser(SignUpRequest request) {
        logger.info("Registering user with email: {}, nickname: {}", request.getEmail(), request.getNickname());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Email already exists: {}", request.getEmail());
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        User user = new User();
        user.setEmail(request.getEmail());

        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // 비밀번호 암호화
        userRepository.save(user);

        logger.info("email:{}",user.getEmail());
    }

    public String login(LoginRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty() || !passwordEncoder.matches(request.getPassword(), optionalUser.get().getPassword())) {
            throw new IllegalArgumentException("잘못된 이메일 또는 비밀번호");
        }
        return jwtTokenProvider.generateToken(optionalUser.get().getEmail());
    }
}
