package com.designteam1.repository;

import com.designteam1.model.Child;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ChildRepositoryMongo implements ChildRepository {

    private final String collectionName = "children";

    @Autowired
    private MongoTemplate mt;

    @Override
    public List<Child> getChildren() {
//        Use if need functionality to query by a particular field
//        Query query = new Query();
        return mt.find(new Query(), Child.class, collectionName);
    }

    @Override
    public Optional<Child> getChild(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Child> childList = mt.find(query, Child.class, collectionName);
        return childList.stream().findFirst();
    }
}
