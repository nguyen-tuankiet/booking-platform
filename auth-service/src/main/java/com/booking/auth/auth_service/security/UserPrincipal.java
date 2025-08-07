package com.booking.auth.auth_service.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.booking.auth.auth_service.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
Chuyển đổi từ entity User (bạn lưu trong DB) sang một object mà Spring Security hiểu (UserDetails).

Lưu danh sách quyền (authorities) gồm role và permission để Spring Security kiểm tra khi phân quyền.

Ẩn bớt các thông tin nhạy cảm khi trả về JSON (dùng @JsonIgnore cho password).
 */
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> {
                    List<GrantedAuthority> roleAuthorities = role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                            .collect(Collectors.toList());
                    roleAuthorities.add(new SimpleGrantedAuthority(role.getName().name()));
                    return roleAuthorities.stream();
                })
                .collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

}
