package com.designteam1.repository;

import com.designteam1.model.Student;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {
    List<Student> getStudents(final String FamilyUnitID, final String checkedIn);

    Optional<Student> getStudent(final String id);

    Student createStudent(Student student);

    Student updateStudent(String id, Student student);

    Student deleteStudent(Student student);

    Student updateCheckedIn(String id, Student student);
}
