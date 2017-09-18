package com.designteam1.security.repository;

import com.designteam1.model.security.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryMongo implements UserRepository {
    private final String collectionName = "user";

    @Autowired
    private MongoTemplate mt;

    @Override
    public Optional<User> findByUsername(String username) {
        final Query query = new Query().addCriteria(Criteria.where("username").is(username));
        System.out.println(query.toString());
        List<User> userList = mt.find(query, User.class, collectionName);
        return userList.stream().findFirst();
    }
}
