package com.designteam1.controller;

import com.designteam1.model.*;
import com.designteam1.repository.FamilyRepository;
import com.designteam1.repository.LineItemRepository;
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

    public StudentController(final StudentRepository studentRepository, final FamilyRepository familyRepository,
                             final LineItemRepository lineItemRepository) {
        this.studentRepository = studentRepository;
        this.familyRepository = familyRepository;
        this.lineItemRepository = lineItemRepository;
    }

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getStudents(@RequestParam(value = "familyUnitID", defaultValue = "", required = false) final String familyUnitID,
                                                @RequestParam(value = "checkedIn", defaultValue = "", required = false) final String checkedIn) {
        try {
            final Students students = new Students();
            final List<Student> studentList = studentRepository.getStudents(familyUnitID, checkedIn, "true");
            if (studentList == null) {
                return new ApiResponse(students).send(HttpStatus.OK);
            }
            students.setStudents(studentList);
            return new ApiResponse(students).send(HttpStatus.OK);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getStudents', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the students");
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getStudent(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Student> student = studentRepository.getStudent(id);
            if (!student.isPresent()) {
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the student you were looking for");
            } else {
                return new ApiResponse(student.get()).send(HttpStatus.OK);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getStudent', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the student");
        }
    }

    @GetMapping(value = "/inactive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getInactiveStudents(@RequestParam(value = "familyUnitID", defaultValue = "", required = false) final String familyUnitID,
                                                        @RequestParam(value = "checkedIn", defaultValue = "", required = false) final String checkedIn) {
        try {
            final Students students = new Students();
            final List<Student> studentList = studentRepository.getStudents(familyUnitID, checkedIn, "false");
            if (studentList == null) {
                return new ApiResponse(students).send(HttpStatus.OK);
            }
            students.setStudents(studentList);
            return new ApiResponse(students).send(HttpStatus.OK);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getStudents', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the inactive students");
        }
    }

    // Notes, middle initial, and birthdate are not required
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createStudent(@RequestBody final Student student) {
        try {
            if (student == null || StringUtils.isBlank(student.getFname()) || StringUtils.isBlank(student.getLname())
                    || StringUtils.isBlank(student.getFamilyUnitID()) || StringUtils.isBlank(String.valueOf(student.isCheckedIn()))
                    || StringUtils.isBlank(student.get_id()) || StringUtils.isBlank(String.valueOf(student.isActive()))) {
                logger.error("Error in 'createStudent': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else {
                Optional<Family> studentFamily = familyRepository.getFamily(student.getFamilyUnitID());
                if (studentFamily.isPresent()) {
                    if (!studentFamily.get().isActive()) {
                        logger.error("Error in 'createStudent': cannot add a student to an inactive family");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot add a student to an inactive family");
                    }
                    studentFamily.get().getStudents().add(student.get_id());
                    Family familyResult = familyRepository.updateFamily(studentFamily.get().get_id(), studentFamily.get());
                    if (familyResult == null) {
                        logger.error("Error in 'createStudent': error adding ID to family record");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding ID to family record");
                    } else {
                        student.setActive(true);
                        Student student1 = studentRepository.createStudent(student);
                        if (student1 == null || student1.get_id() == null) {
                            logger.error("Error in 'createStudent': error creating student");
                            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the student");
                        } else {
                            HttpHeaders header = new HttpHeaders();
                            header.add("location", student1.get_id());
                            return new ApiResponse().send(HttpStatus.CREATED);
                        }
                    }
                } else {
                    logger.error("Error in 'createStudent': could not find family associated to student");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the family associated to the student");
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createStudent', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the student");
        }
    }

    @PostMapping(value = "enrollStudent", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> enrollStudent(@RequestBody final Student student) {
        try {
            if (student == null || StringUtils.isBlank(student.getFname()) || StringUtils.isBlank(student.getLname())
                    || StringUtils.isBlank(student.getFamilyUnitID()) || StringUtils.isBlank(String.valueOf(student.isCheckedIn()))
                    || StringUtils.isBlank(student.get_id()) || StringUtils.isBlank(String.valueOf(student.isActive()))) {
                logger.error("Error in 'createStudent': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else {
                student.setActive(true);
                Student student1 = studentRepository.createStudent(student);
                if (student1 == null || student1.get_id() == null) {
                    logger.error("Error in 'createStudent': error enrolling student");
                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while enrolling the student");
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", student1.get_id());
                    return new ApiResponse().send(HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createStudent', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while enrolling the guardian");
        }
    }

    // Notes, middle initial, checkedIn, and birthdate are not required
    @PutMapping(value = "updateStudent/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateStudent(@PathVariable(name = "id") final String id, @RequestBody final Student student) {
        try {
            if (student == null || id == null || StringUtils.isBlank(student.getFname()) || StringUtils.isBlank(student.getLname())
                    || StringUtils.isBlank(student.getFamilyUnitID()) || StringUtils.isBlank(student.get_id()) ||
                    StringUtils.isBlank(String.valueOf(student.isActive())) || StringUtils.isBlank(String.valueOf(student.isCheckedIn()))) {
                logger.error("Error in 'updateStudent': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (!id.equals(student.get_id())) {
                logger.error("Error in 'updateStudent': id parameter does not match id in student");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in student");
            } else {
                Optional<Student> studentOptional = studentRepository.getStudent(id);
                if (!studentOptional.isPresent()) {
                    logger.error("Error in 'updateStudent': could not find student to update");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Cannot find the student you were trying to update");
                } else {
                    if (!studentOptional.get().isActive()) {
                        logger.error("Error in 'updateStudent': cannot update an inactive student");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot update an inactive student");
                    }
                    Student result = studentRepository.updateStudent(id, student);
                    if (result == null) {
                        logger.error("Error in 'updateStudent': error building student");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the student");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateStudent', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the student");
        }
    }

    @PutMapping(value = "updateCheckedIn/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateCheckedIn(@PathVariable(name = "id") final String id, @RequestBody final Student student) {
        try {
            if (student == null || id == null || StringUtils.isBlank(student.getFname()) || StringUtils.isBlank(student.getLname())
                    || StringUtils.isBlank(student.getFamilyUnitID()) || StringUtils.isBlank(student.get_id()) ||
                    StringUtils.isBlank(String.valueOf(student.isActive())) || StringUtils.isBlank(String.valueOf(student.isCheckedIn()))) {
                logger.error("Error in 'updateCheckedIn': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (!id.equals(student.get_id())) {
                logger.error("Error in 'updateCheckedIn': id parameter does not match id in student");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in student");
            } else {
                Optional<Student> studentOptional = studentRepository.getStudent(id);
                if (!studentOptional.isPresent()) {
                    logger.error("Error in 'updateCheckedIn': tried to check in/out a student that does not exist");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the student you were trying to check in/out");
                } else {
                    if (!studentOptional.get().isActive()) {
                        logger.error("Error in 'updateCheckedIn': cannot check in an inactive student");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot check in an inactive student");
                    }
                    Student result = studentRepository.updateCheckedIn(id, student);
                    if (result == null) {
                        logger.error("Error in 'updateCheckedIn': error building student");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while checking in/out the student");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateCheckedIn', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while checking in/out the student");
        }
    }

//    @DeleteMapping(value = "{id}")
//    public ResponseEntity<Void> deleteStudent(@PathVariable(name = "id") final String id) {
//        try {
//            Optional<Student> student = studentRepository.getStudent(id);
//            if (student.isPresent()) {
//                Optional<Family> studentFamily = familyRepository.getFamily(student.get().getFamilyUnitID());
//                if (studentFamily.isPresent()) {
//                    if (studentFamily.get().getStudents().size() == 1) {
//                        logger.error("Error in 'deleteStudent': a family must have at least 1 child");
//                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//                    }
//                    List<LineItem> uninvoicedLineItems = lineItemRepository.getLineItems(null, student.get().get_id(),
//                            null, "null", null, null, null, null);
//                    if (uninvoicedLineItems.size() > 0) {
//                        logger.error("Error in 'deleteStudent': you cannot delete a student with uninvoiced line items");
//                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//                    }
//                    // Remove student ID from student array in family
//                    studentFamily.get().getStudents().removeIf(s -> s.equals(student.get().get_id()));
//                    Family familyResult = familyRepository.updateFamily(studentFamily.get().get_id(), studentFamily.get());
//                    if (familyResult == null) {
//                        logger.error("Error in 'deleteStudent': error updating family record");
//                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//                    } else {
//                        Student result = studentRepository.deleteStudent(student.get());
//                        if (result == null) {
//                            logger.error("Error in 'deleteStudent': error deleting student");
//                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//                        } else {
//                            return ResponseEntity.status(HttpStatus.OK).body(null);
//                        }
//                    }
//                } else {
//                    logger.error("Error in 'deleteStudent': cannot find family associated to student");
//                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//                }
//            } else {
//                logger.error("Error in 'deleteStudent': student is null");
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//        } catch (final Exception e) {
//            logger.error("Caught " + e + " in 'deleteStudent', " + e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

    @PutMapping(value = "/updateActive/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateActiveStudent(@PathVariable(name = "id") final String id, @RequestBody final Student student) {
        try {
            if (student == null || id == null || StringUtils.isBlank(student.getFname()) || StringUtils.isBlank(student.getLname())
                    || StringUtils.isBlank(student.getFamilyUnitID()) || StringUtils.isBlank(student.get_id()) ||
                    StringUtils.isBlank(String.valueOf(student.isActive())) || StringUtils.isBlank(String.valueOf(student.isCheckedIn()))) {
                logger.error("Error in 'updateActiveStudent': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (!id.equals(student.get_id())) {
                logger.error("Error in 'updateActiveStudent': id parameter does not match id in student");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in student");
            } else {
                Optional<Student> studentOptional = studentRepository.getStudent(id);
                if (!studentOptional.isPresent()) {
                    logger.error("Error in 'updateActiveStudent': tried to update a student that does not exist");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the student you were trying to update");
                } else {
                    List<LineItem> uninvoicedLineItems = lineItemRepository.getLineItems(null, studentOptional.get().get_id(),
                            null, "null", null, null, null, null);
                    if (studentOptional.get().isActive() && !student.isActive() && uninvoicedLineItems.size() > 0) {
                        logger.error("Error in 'updateActiveStudent': you cannot deactivate a student with uninvoiced line items");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot deactivate a student with uninvoiced line items");
                    }
                    List<Student> activeStudents = studentRepository.getStudents(studentOptional.get().getFamilyUnitID(), null, "true");
                    if (studentOptional.get().isActive() && !student.isActive() && activeStudents.size() == 1) {
                        logger.error("Error in 'updateActiveStudent': a family must have at least 1 active student");
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, "A family must have at least 1 active student");
                    }
                    Student result = studentRepository.updateActive(id, student);
                    if (result == null) {
                        logger.error("Error in 'updateActiveStudent': error building student");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the student");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateActiveStudent', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the student");
        }
    }
}
