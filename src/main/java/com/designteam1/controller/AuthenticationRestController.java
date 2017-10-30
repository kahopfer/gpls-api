package com.designteam1.controller;

import com.designteam1.security.JwtAuthenticationRequest;
import com.designteam1.security.JwtTokenUtil;
import com.designteam1.security.JwtUser;
import com.designteam1.service.JwtAuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@CrossOrigin
@RestController
public class AuthenticationRestController {

    @Value("Authorization")
    private String tokenHeader;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @RequestMapping(value = "auth", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, Device device) throws AuthenticationException {

        // Reload password post-security so we can generate token
        final JwtUser userDetails = (JwtUser) userDetailsService.loadUserByUsername(authenticationRequest.getUsername().toUpperCase());

        //Compare auth request
        if (authenticationRequest.getPassword().equals(userDetails.getPassword())) {
            final String token = jwtTokenUtil.generateToken(userDetails, device);

            // Return the token
            return ResponseEntity.ok(new JwtAuthenticationResponse(token, userDetails.getUsername().toUpperCase(), userDetails.getFirstname(), userDetails.getLastname(), userDetails.getAuthorities()));
        } else {
            return new ResponseEntity<>(Collections.singletonMap("response", "username or password is incorrect"), HttpStatus.UNAUTHORIZED);
        }
    }

    //    Do not include "bearer" with token
    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return ResponseEntity.ok(new JwtAuthenticationResponse(refreshedToken, username, null, null, null));
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
