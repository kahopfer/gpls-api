package com.designteam1.security;

import com.designteam1.model.AuthorityName;
import com.designteam1.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(User user) {
        return new JwtUser(
                user.getId(),
                user.getUsername(),
                user.getFirstname(),
                user.getLastname(),
                user.getPassword(),
                mapToGrantedAuthorities(user.getAuthorities()),
                user.getLastPasswordResetDate()
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<AuthorityName> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.name()))
                .collect(Collectors.toList());
    }
}
