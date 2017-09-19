package com.designteam1.repository;

import com.designteam1.model.Family;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FamilyRepositoryMongo implements FamilyRepository {
    private final String collectionName = "Family";

    @Autowired
    private MongoTemplate mt;

    @Override
    public List<Family> getFamilies() {
        return mt.find(new Query(), Family.class, collectionName);
    }

    @Override
    public Optional<Family> getFamily(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Family> familyList = mt.find(query, Family.class, collectionName);
        return familyList.stream().findFirst();
    }
}
