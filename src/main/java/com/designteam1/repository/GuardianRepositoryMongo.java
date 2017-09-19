package com.designteam1.repository;

import com.designteam1.model.Guardian;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GuardianRepositoryMongo implements GuardianRepository {

    private final String collectionName = "Guardian";

    @Autowired
    private MongoTemplate mt;


    @Override
    public List<Guardian> getGuardians(final String familyUnitID) {
        Query query = new Query();

        if (StringUtils.isNotEmpty(familyUnitID)) {
            ObjectId objID = new ObjectId(familyUnitID);
            query.addCriteria(Criteria.where("familyUnitID").is(objID));
        }
        System.out.println(query.toString());
        return mt.find(query, Guardian.class, collectionName);
    }

    @Override
    public Optional<Guardian> getGuardian(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Guardian> guardianList = mt.find(query, Guardian.class, collectionName);
        return guardianList.stream().findFirst();
    }
}
