package com.designteam1.security.repository;

import com.designteam1.model.security.User;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.Optional;

///**
// * Created by stephan on 20.03.16.
// */
//@Document(collection = "user")
//@Repository
//@RepositoryRestResource(exported = false)
//public interface UserRepository {
//    Optional<User> findByUsername(String username);
//}
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
}


