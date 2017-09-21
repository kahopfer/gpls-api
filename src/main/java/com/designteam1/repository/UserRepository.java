package com.designteam1.repository;

import com.designteam1.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getUsers();

    Optional<User> findByUsername(String username);

    Optional<User> getUser(final String id);

    User createUser(User user);

    User updateUser(String username, User user);

    User deleteUser(User user);
}


