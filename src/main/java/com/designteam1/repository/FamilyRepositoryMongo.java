package com.designteam1.repository;

import com.designteam1.model.Family;
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

    @Override
    public Family createFamily(Family family) {
//        family.set_id(null);
        mt.save(family, collectionName);
        return family;
    }

    @Override
    public Family deleteFamily(Family family) {
        mt.remove(family, collectionName);
        return family;
    }

    @Override
    public Family updateFamily(String id, Family family) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("familyName", family.getFamilyName());
        update.set("students", family.getStudents());
        update.set("guardians", family.getGuardians());

        final WriteResult result = mt.updateFirst(query, update, Family.class, collectionName);

        if (result != null) {
            return family;
        } else {
            return null;
        }
    }
}
