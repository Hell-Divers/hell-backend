package com.hell.backend.common.security;

import com.hell.backend.users.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 추가 메서드: 사용자 ID 가져오기
    public Long getId() {
        return user.getId();
    }

    // UserDetails 인터페이스 메서드 구현
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한 설정 (필요에 따라 수정)
        return null; // 또는 적절한 권한 리스트 반환
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
