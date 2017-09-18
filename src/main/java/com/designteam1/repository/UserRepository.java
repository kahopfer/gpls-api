package com.designteam1.repository;

import com.designteam1.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);

    User createUser(User user);

    User updateUser(String id, User user);

    User deleteUser(User user);
}


