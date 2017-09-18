package com.designteam1.service;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

public class JwtAuthenticationResponse implements Serializable {

    private static final long serialVersionUID = 1250166508152483573L;

    private final String token;
    private final String username;
    private final String firstname;
    private final String lastname;
    private final Collection<? extends GrantedAuthority> authorities;


    public JwtAuthenticationResponse(String token, String username, String firstname, String lastname, Collection<? extends GrantedAuthority> authorities) {
        this.token = token;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.authorities = authorities;
    }

    public String getToken() {
        return this.token;
    }

    public String getUsername() {
        return username;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }
}
