package com.designteam1.repository;

import com.designteam1.model.User;
import com.mongodb.WriteResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
    public Optional<User> getUser(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
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
    public User updateUser(String username, User user) {
        final Query query = new Query().addCriteria(Criteria.where("username").is(username));
        final Update update = new Update();

        update.set("password", user.getPassword());
        update.set("lastPasswordResetDate", user.getLastPasswordResetDate());

        final WriteResult result = mt.updateFirst(query, update, User.class, collectionName);

        if (result != null) {
            return user;
        } else {
            return null;
        }
    }

    @Override
    public User deleteUser(User user) {
        mt.remove(user, collectionName);
        return user;
    }
}
