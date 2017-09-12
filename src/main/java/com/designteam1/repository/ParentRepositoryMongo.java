package com.designteam1.repository;

import com.designteam1.model.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ParentRepositoryMongo implements ParentRepository {

    private final String collectionName = "parents";

    @Autowired
    private MongoTemplate mt;


    @Override
    public List<Parent> getParents() {
//        Use if need functionality to query by a particular field
//        Query query = new Query();
        return mt.find(new Query(), Parent.class, collectionName);
    }

    @Override
    public Optional<Parent> getParent(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Parent> parentList = mt.find(query, Parent.class, collectionName);
        return parentList.stream().findFirst();
    }
}
