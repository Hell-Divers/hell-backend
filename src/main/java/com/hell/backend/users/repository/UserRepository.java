package com.hell.backend.users.repository;

import com.hell.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //중복 이메일 검사
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
