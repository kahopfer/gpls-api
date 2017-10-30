package com.designteam1.controller;

import com.designteam1.model.User;
import com.designteam1.model.Users;
import com.designteam1.repository.UserRepository;
import com.designteam1.security.JwtTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController() {

    }

    public UserController(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Users> getUsers() {
        try {
            final Users users = new Users();
            final List<User> userList = userRepository.getUsers();
            if (userList == null) {
                return ResponseEntity.ok(users);
            }
            users.setUsers(userList);
            return ResponseEntity.ok(users);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getUsers', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> createUser(@RequestBody final User user) {
        try {
            if (user == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword()) ||
                    StringUtils.isBlank(user.getFirstname()) || StringUtils.isBlank(user.getLastname()) || user.getAuthorities() == null || user.getAuthorities().isEmpty()) {
                logger.error("Error in 'createUser': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                user.setUsername(user.getUsername().toUpperCase());
                Optional<User> userList = userRepository.findByUsername(user.getUsername());
                if (userList.isPresent()) {
                    logger.error("Error in 'createUser': username already exists");
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
                }

                user.setLastPasswordResetDate(new Date());
                User user1 = userRepository.createUser(user);
                if (user1 == null || user1.getId() == null) {
                    logger.error("Error in 'createUser': error creating user");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", user1.getId());
                    return new ResponseEntity<User>(null, header, HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createUser', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping(value = "{userToDelete}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "userToDelete") final String userToDelete, @RequestHeader("Authorization") String authToken) {
        try {
            // No need to check for auth token. API will automatically return a 401 if it is not provided in the request header
            if (authToken.startsWith("Bearer ")) {
                authToken = authToken.substring(7);
            }
            String myUsername = jwtTokenUtil.getUsernameFromToken(authToken);

            Optional<User> user = userRepository.findByUsername(userToDelete);
            if (user.isPresent() && !userToDelete.equals(myUsername)) {
                User result = userRepository.deleteUser(user.get());
                if (result == null) {
                    logger.error("Error in 'deleteUser': error deleting user");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(null);
                }
            } else if (userToDelete.equals(myUsername)) {
                logger.error("Error in 'deleteUser': user tried to delete themselves");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                logger.error("Error in 'deleteUser': user is null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteUser', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    //TODO: Make sure this logic is sound
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> changePassword(@RequestHeader("Old-Password") final String oldPassword,
                                               @RequestBody final User user, @RequestHeader("Authorization") String authToken) {
        try {
            // No need to check for auth token. API will automatically return a 401 if it is not provided in the request header
            if (authToken.startsWith("Bearer ")) {
                authToken = authToken.substring(7);
            }

            String userToUpdate = jwtTokenUtil.getUsernameFromToken(authToken);

            if (user == null || oldPassword == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
                logger.error("Error in 'changePassword': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!userToUpdate.equals(user.getUsername())) {
                logger.error("Error in 'changePassword': username in auth token does not match username in user");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            } else {
                Optional<User> userOptional = userRepository.findByUsername(userToUpdate);
                if (!userOptional.isPresent()) {
                    logger.error("Error in 'changePassword': tried to update a user that does not exist");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                } else if (!userOptional.get().getPassword().equals(oldPassword)) {
                    logger.error("Error in 'changePassword': old password was incorrect");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                } else {
                    user.setLastPasswordResetDate(new Date());
                    User result = userRepository.updateUser(userToUpdate, user);
                    if (result == null) {
                        logger.error("Error in 'changePassword': error building user");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'changePassword', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping(value = "resetPassword/{userToUpdate}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> resetPassword(@PathVariable(name = "userToUpdate") final String userToUpdate, @RequestBody final User user) {
        try {
            if (user == null || userToUpdate == null || StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())) {
                logger.error("Error in 'resetPassword': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if(!userToUpdate.equals(user.getUsername())) {
                logger.error("Error in 'resetPassword': username parameter does not match username in user");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<User> userOptional = userRepository.findByUsername(userToUpdate);
                if (!userOptional.isPresent()) {
                    logger.error("Error in 'resetPassword': tried to update a user that does not exist");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                } else {
                    user.setLastPasswordResetDate(new Date());
                    User result = userRepository.updateUser(userToUpdate, user);
                    if (result == null) {
                        logger.error("Error in 'resetPassword': error building user");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'resetPassword', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
