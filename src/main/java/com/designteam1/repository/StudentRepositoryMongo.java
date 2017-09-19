package com.designteam1.repository;

import com.designteam1.model.Student;
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
public class StudentRepositoryMongo implements StudentRepository {

    private final String collectionName = "Student";

    @Autowired
    private MongoTemplate mt;

    @Override
    public List<Student> getStudents(final String familyUnitID) {
        Query query = new Query();

        if (StringUtils.isNotEmpty(familyUnitID)) {
            ObjectId objID = new ObjectId(familyUnitID);
            query.addCriteria(Criteria.where("familyUnitID").is(objID));
        }
        return mt.find(query, Student.class, collectionName);
    }

    @Override
    public Optional<Student> getStudent(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Student> studentList = mt.find(query, Student.class, collectionName);
        return studentList.stream().findFirst();
    }
}
