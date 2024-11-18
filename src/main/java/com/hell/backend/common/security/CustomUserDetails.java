package com.hell.backend.common.security;

import com.hell.backend.users.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user; // User 엔티티 객체를 포함

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // User 엔티티의 ID를 가져오는 메서드
    public Long getId() {
        return user.getId();
    }

    // 아래는 UserDetails 인터페이스의 메서드 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 설정 (필요에 따라 수정)
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 또는 다른 고유 식별자
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // 계정 만료 여부 (필요에 따라 수정)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 계정 잠금 여부 (필요에 따라 수정)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 자격 증명 만료 여부 (필요에 따라 수정)
    }

    @Override
    public boolean isEnabled() {
        return true; // 계정 활성화 여부 (필요에 따라 수정)
    }
}
