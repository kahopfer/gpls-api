package com.designteam1.repository;

import com.designteam1.model.Student;
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
public class StudentRepositoryMongo implements StudentRepository {

    private final String collectionName = "Student";

    @Autowired
    private MongoTemplate mt;

    @Override
    public List<Student> getStudents(final String familyUnitID, final String checkedIn) {
        Query query = new Query();

        if (StringUtils.isNotEmpty(familyUnitID)) {
            query.addCriteria(Criteria.where("familyUnitID").is(familyUnitID));
        }
        if (StringUtils.isNotEmpty(checkedIn)) {
            Boolean checkedIn1 = Boolean.valueOf(checkedIn);
            query.addCriteria(Criteria.where("checkedIn").is(checkedIn1));
        }
        return mt.find(query, Student.class, collectionName);
    }

    @Override
    public Optional<Student> getStudent(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Student> studentList = mt.find(query, Student.class, collectionName);
        return studentList.stream().findFirst();
    }

    @Override
    public Student createStudent(Student student) {
//        student.set_id(null);
        mt.save(student, collectionName);
        return student;
    }

    @Override
    public Student updateStudent(String id, Student student) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("fname", student.getFname());
        update.set("lname", student.getLname());
        update.set("mi", student.getMi());
//        update.set("birthdate", student.getBirthdate());
        update.set("notes", student.getNotes());
        update.set("familyUnitID", student.getFamilyUnitID());

        final WriteResult result = mt.updateFirst(query, update, Student.class, collectionName);

        if (result != null) {
            return student;
        } else {
            return null;
        }
    }

    @Override
    public Student deleteStudent(Student student) {
        mt.remove(student, collectionName);
        return student;
    }

    @Override
    public Student updateCheckedIn(String id, Student student) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("checkedIn", student.isCheckedIn());

        final WriteResult result = mt.updateFirst(query, update, Student.class, collectionName);

        if (result != null) {
            return student;
        } else {
            return null;
        }
    }
}
