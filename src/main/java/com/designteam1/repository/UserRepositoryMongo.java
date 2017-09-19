package com.designteam1.repository;

import com.designteam1.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryMongo implements UserRepository {
    private final String collectionName = "User";

    @Autowired
    private MongoTemplate mt;

    @Override
    public List<User> getUsers() {
        return mt.find(new Query(), User.class, collectionName);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        final Query query = new Query().addCriteria(Criteria.where("username").is(username));
        List<User> userList = mt.find(query, User.class, collectionName);
        return userList.stream().findFirst();
    }

    @Override
    public User createUser(User user) {
        user.setId(null);
        mt.save(user, collectionName);
        return user;
    }

    @Override
    public User updateUser(String id, User user) {
        return null;
    }

    @Override
    public User deleteUser(User user) {
        return null;
    }
}