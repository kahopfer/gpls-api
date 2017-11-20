package com.designteam1.repository;

import com.designteam1.model.Guardian;
import com.mongodb.WriteResult;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class GuardianRepositoryMongo implements GuardianRepository {

    private final String collectionName = "Guardian";

    @Autowired
    private MongoTemplate mt;


    @Override
    public List<Guardian> getGuardians(final String familyUnitID, final String active) {
        Query query = new Query();

        if (StringUtils.isNotEmpty(familyUnitID)) {
//            ObjectId objID = new ObjectId(familyUnitID);
//            query.addCriteria(Criteria.where("familyUnitID").is(objID));
            query.addCriteria(Criteria.where("familyUnitID").is(familyUnitID));
        }
        if (StringUtils.isNotEmpty(active)) {
            Boolean active1 = Boolean.valueOf(active);
            query.addCriteria(Criteria.where("active").is(active1));
        }
        return mt.find(query, Guardian.class, collectionName);
    }

    @Override
    public Optional<Guardian> getGuardian(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Guardian> guardianList = mt.find(query, Guardian.class, collectionName);
        return guardianList.stream().findFirst();
    }

    @Override
    public Guardian createGuardian(Guardian guardian) {
//        guardian.set_id(null);
        mt.save(guardian, collectionName);
        return guardian;
    }

    @Override
    public Guardian updateGuardian(String id, Guardian guardian) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("fname", guardian.getFname());
        update.set("lname", guardian.getLname());
        update.set("mi", guardian.getMi());
        update.set("relationship", guardian.getRelationship());
        update.set("primPhone", guardian.getPrimPhone());
        update.set("secPhone", guardian.getSecPhone());
        update.set("email", guardian.getEmail());
        update.set("familyUnitID", guardian.getFamilyUnitID());

        final WriteResult result = mt.updateFirst(query, update, Guardian.class, collectionName);

        if (result != null) {
            return guardian;
        } else {
            return null;
        }
    }

    @Override
    public Guardian deleteGuardian(Guardian guardian) {
        mt.remove(guardian, collectionName);
        return guardian;
    }

    @Override
    public Guardian updateActive(String id, Guardian guardian) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("active", guardian.isActive());

        final WriteResult result = mt.updateFirst(query, update, Guardian.class, collectionName);

        if (result != null) {
            return guardian;
        } else {
            return null;
        }
    }
}
