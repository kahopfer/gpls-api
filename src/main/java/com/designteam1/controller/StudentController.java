package com.designteam1.controller;

import com.designteam1.model.Student;
import com.designteam1.model.Students;
import com.designteam1.repository.StudentRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("students")
public class StudentController {
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    public StudentController() {

    }

    public StudentController(final StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Students> getStudents(@RequestParam(value = "familyUnitID", defaultValue = "", required = false) final String familyUnitID,
                                                @RequestParam(value = "checkedIn", defaultValue = "", required = false) final String checkedIn) {
        try {
            final Students students = new Students();
            final List<Student> studentList = studentRepository.getStudents(familyUnitID, checkedIn);
            if (studentList == null) {
                return ResponseEntity.ok(students);
            }
            students.setStudents(studentList);
            return ResponseEntity.ok(students);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getStudents', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Student> getStudent(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Student> student = studentRepository.getStudent(id);
            if (!student.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(student.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getStudent', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Notes, middle initial, and birthdate are not required
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Student> createStudent(@RequestBody final Student student) {
        try {
            if (student == null || StringUtils.isBlank(student.getFname()) || StringUtils.isBlank(student.getLname())
                    || StringUtils.isBlank(student.getFamilyUnitID()) || StringUtils.isBlank(String.valueOf(student.isCheckedIn()))
                    || StringUtils.isBlank(student.get_id())) {
                logger.error("Error in 'createStudent': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Student student1 = studentRepository.createStudent(student);
                if (student1 == null || student1.get_id() == null) {
                    logger.error("Error in 'createStudent': error creating student");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", student1.get_id());
                    return new ResponseEntity<Student>(null, header, HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createStudent', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Notes, middle initial, checkedIn, and birthdate are not required
    @PutMapping(value = "updateStudent/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Student> updateStudent(@PathVariable(name = "id") final String id, @RequestBody final Student student) {
        try {
            if (student == null || id == null || StringUtils.isBlank(student.getFname()) || StringUtils.isBlank(student.getLname())
                    || StringUtils.isBlank(student.getFamilyUnitID()) || StringUtils.isBlank(student.get_id())) {
                logger.error("Error in 'updateStudent': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(student.get_id())) {
                logger.error("Error in 'updateStudent': id parameter does not match id in student");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Student> studentOptional = studentRepository.getStudent(id);
                if (!studentOptional.isPresent()) {
                    return this.createStudent(student);
                } else {
                    Student result = studentRepository.updateStudent(id, student);
                    if (result == null) {
                        logger.error("Error in 'updateStudent': error building student");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateStudent', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping(value = "updateCheckedIn/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Student> updateCheckedIn(@PathVariable(name = "id") final String id, @RequestBody final Student student) {
        try {
            if (student == null || id == null || StringUtils.isBlank(String.valueOf(student.isCheckedIn())) || StringUtils.isBlank(student.get_id())) {
                logger.error("Error in 'updateCheckedIn': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(student.get_id())) {
                logger.error("Error in 'updateCheckedIn': id parameter does not match id in student");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Student> studentOptional = studentRepository.getStudent(id);
                if (!studentOptional.isPresent()) {
                    logger.error("Error in 'updateCheckedIn': tried to check in/out a student that does not exist");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                } else {
                    Student result = studentRepository.updateCheckedIn(id, student);
                    if (result == null) {
                        logger.error("Error in 'updateCheckedIn': error building student");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateCheckedIn', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable(name = "id") final String id) {
        try {
            Optional<Student> student = studentRepository.getStudent(id);
            if (student.isPresent()) {
                Student result = studentRepository.deleteStudent(student.get());
                if (result == null) {
                    logger.error("Error in 'deleteStudent': error deleting student");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(null);
                }
            } else {
                logger.error("Error in 'deleteStudent': student is null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteStudent', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
