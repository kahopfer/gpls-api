package com.designteam1.repository;

import com.designteam1.model.User;

import java.util.Optional;


//@Repository
//public interface UserRepository extends MongoRepository<User, String> {
//    User findByUsername(String username);
//}

public interface UserRepository {
    Optional<User> findByUsername(String username);
}


