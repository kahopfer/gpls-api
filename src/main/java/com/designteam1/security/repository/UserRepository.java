package com.designteam1.security.repository;

import com.designteam1.model.security.User;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;


//@Repository
//public interface UserRepository extends MongoRepository<User, String> {
//    User findByUsername(String username);
//}

public interface UserRepository {
    Optional<User> findByUsername(String username);
}


