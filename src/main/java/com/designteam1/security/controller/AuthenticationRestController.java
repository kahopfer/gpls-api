package com.designteam1.security.controller;

import com.designteam1.security.JwtAuthenticationRequest;
import com.designteam1.security.JwtTokenUtil;
import com.designteam1.security.JwtUser;
import com.designteam1.security.service.JwtAuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@CrossOrigin
@RestController
public class AuthenticationRestController {

//    @Value("${jwt.header}")
    @Value("Authorization")
    private String tokenHeader;

//    @Autowired
//    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

//    @RequestMapping(value = "${jwt.route.authentication.path}", method = RequestMethod.POST)
    @RequestMapping(value = "auth", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest, Device device) throws AuthenticationException {

        // Perform the security
//        final Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        authenticationRequest.getUsername(),
//                        authenticationRequest.getPassword()
//                )
//        );
//        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        //Compare auth request
        if(authenticationRequest.getPassword().equals(userDetails.getPassword())) {
            final String token = jwtTokenUtil.generateToken(userDetails, device);

            // Return the token
            return ResponseEntity.ok(new JwtAuthenticationResponse(token, userDetails.getUsername()));
        } else {
            return new ResponseEntity<>(Collections.singletonMap("response", "username or password is incorrect"), HttpStatus.UNAUTHORIZED);
        }
    }

//    Do not include "bearer" with token
//    @RequestMapping(value = "${jwt.route.authentication.refresh}", method = RequestMethod.GET)
    @RequestMapping(value = "refresh", method = RequestMethod.GET)
    public ResponseEntity<?> refreshAndGetAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

        if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
            String refreshedToken = jwtTokenUtil.refreshToken(token);
            return ResponseEntity.ok(new JwtAuthenticationResponse(refreshedToken, username));
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

}
