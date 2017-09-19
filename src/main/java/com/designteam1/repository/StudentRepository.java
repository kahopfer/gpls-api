package com.designteam1.repository;

import com.designteam1.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    List<Student> getStudents(final String FamilyUnitID);

    Optional<Student> getStudent(final String id);

    //TODO: Add create, update, and delete
}
