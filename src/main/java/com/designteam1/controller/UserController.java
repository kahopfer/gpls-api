package com.designteam1.controller;

import com.designteam1.model.User;
import com.designteam1.model.Users;
import com.designteam1.repository.UserRepository;
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

    @DeleteMapping(value = "/userToDelete/{userToDelete}/myUsername/{myUsername}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name = "userToDelete") final String userToDelete, @PathVariable(name = "myUsername") final String myUsername) {
        try {
            Optional<User> user = userRepository.findByUsername(userToDelete);
            if(user.isPresent() && !userToDelete.equals(myUsername)) {
                User result = userRepository.deleteUser(user.get());
                if(result == null) {
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
}
