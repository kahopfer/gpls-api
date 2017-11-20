package com.designteam1.controller;

import com.designteam1.model.*;
import com.designteam1.repository.*;
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
@RequestMapping("families")
public class FamilyController {
    private static final Logger logger = LoggerFactory.getLogger(FamilyController.class);

    public FamilyController() {

    }

    public FamilyController(final FamilyRepository familyRepository, final StudentRepository studentRepository,
                            final GuardianRepository guardianRepository, final InvoiceRepository invoiceRepository,
                            final LineItemRepository lineItemRepository) {
        this.familyRepository = familyRepository;
        this.studentRepository = studentRepository;
        this.guardianRepository = guardianRepository;
        this.invoiceRepository = invoiceRepository;
        this.lineItemRepository = lineItemRepository;
    }

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GuardianRepository guardianRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Families> getFamilies() {
        try {
            final Families families = new Families();
            final List<Family> familyList = familyRepository.getFamilies("true");
            if (familyList == null) {
                return ResponseEntity.ok(families);
            }
            families.setFamilies(familyList);
            return ResponseEntity.ok(families);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getFamilies', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Family> getFamily(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Family> family = familyRepository.getFamily(id);
            if (!family.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(family.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getFamily', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "/inactive", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Families> getInactiveFamilies() {
        try {
            final Families families = new Families();
            final List<Family> familyList = familyRepository.getFamilies("false");
            if (familyList == null) {
                return ResponseEntity.ok(families);
            }
            families.setFamilies(familyList);
            return ResponseEntity.ok(families);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getFamilies', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Family> createFamily(@RequestBody final Family family) {
        try {
            if (family == null || StringUtils.isBlank(family.getFamilyName()) || family.getGuardians() == null || family.getGuardians().isEmpty() ||
                    StringUtils.isBlank(family.get_id()) || family.getStudents() == null || family.getStudents().isEmpty() ||
                    StringUtils.isBlank(String.valueOf(family.isActive()))) {
                logger.error("Error in 'createFamily': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                family.setActive(true);
                Family family1 = familyRepository.createFamily(family);
                if (family1 == null || family1.get_id() == null) {
                    logger.error("Error in 'createFamily': error creating family");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", family1.get_id());
                    return new ResponseEntity<Family>(null, header, HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createFamily', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Family> updateFamily(@PathVariable(name = "id") final String id, @RequestBody final Family family) {
        try {
            if (family == null || id == null || StringUtils.isBlank(family.get_id()) ||
                    StringUtils.isBlank(family.getFamilyName()) || family.getGuardians() == null || family.getGuardians().isEmpty() ||
                    StringUtils.isBlank(family.get_id()) || family.getStudents() == null || family.getStudents().isEmpty() ||
                    StringUtils.isBlank(String.valueOf(family.isActive()))) {
                logger.error("Error in 'updateFamily': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(family.get_id())) {
                logger.error("Error in 'updateFamily': id parameter does not match id in family");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Family> familyOptional = familyRepository.getFamily(id);
                if (!familyOptional.isPresent()) {
                    logger.error("Error in 'updateFamily': could not find family to update");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                } else {
                    if (!familyOptional.get().isActive()) {
                        logger.error("Error in 'updateFamily': cannot update an inactive family");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    Family result = familyRepository.updateFamily(id, family);
                    if (result == null) {
                        logger.error("Error in 'updateFamily': error building family");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateFamily', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteFamily(@PathVariable(name = "id") final String id) {
        try {
            Optional<Family> family = familyRepository.getFamily(id);
            if (family.isPresent()) {
                // Check for uninvoiced line items
                List<LineItem> uninvoicedLineItemList = lineItemRepository.getLineItems(family.get().get_id(), null,
                        null, "null", null, null, null, null);
                if (!uninvoicedLineItemList.isEmpty()) {
                    logger.error("Error in 'deleteFamily': you cannot delete a family with uninvoiced line items");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                // Check for unpaid invoices
                List<Invoice> unpaidInvoiceList = invoiceRepository.getInvoices(family.get().get_id(), "false");
                if (!unpaidInvoiceList.isEmpty()) {
                    logger.error("Error in 'deleteFamily': you cannot delete a family with unpaid invoices");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                // Delete line items
                List<LineItem> lineItemList = lineItemRepository.getLineItems(family.get().get_id(), null,
                        null, null, null, null, null, null);
                for (LineItem lineItem : lineItemList) {
                    LineItem result = lineItemRepository.deleteLineItem(lineItem);
                    if (result == null) {
                        logger.error("Error in 'deleteFamily': error deleting line item");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    }
                }
                // Delete invoices
                List<Invoice> invoiceList = invoiceRepository.getInvoices(family.get().get_id(), null);
                for (Invoice invoice : invoiceList) {
                    Invoice result = invoiceRepository.deleteInvoice(invoice);
                    if (result == null) {
                        logger.error("Error in 'deleteFamily': error deleting invoice");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    }
                }
                // Delete guardians
                for (String guardianID : family.get().getGuardians()) {
                    Optional<Guardian> guardian = guardianRepository.getGuardian(guardianID);
                    if (guardian.isPresent()) {
                        Guardian result = guardianRepository.deleteGuardian(guardian.get());
                        if (result == null) {
                            logger.error("Error in 'deleteFamily': error deleting guardian");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                        }
                    } else {
                        logger.error("Error in 'deleteFamily': guardian is null");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    }
                }
                // Delete students
                for (String studentID : family.get().getStudents()) {
                    Optional<Student> student = studentRepository.getStudent(studentID);
                    if (student.isPresent()) {
                        Student result = studentRepository.deleteStudent(student.get());
                        if (result == null) {
                            logger.error("Error in 'deleteFamily': error deleting student");
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                        }
                    } else {
                        logger.error("Error in 'deleteFamily': student is null");
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    }
                }
                // Delete family
                Family result = familyRepository.deleteFamily(family.get());
                if (result == null) {
                    logger.error("Error in 'deleteFamily': error deleting family");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(null);
                }
            } else {
                logger.error("Error in 'deleteFamily': family is null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteFamily', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping(value = "/updateActive/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Family> updateActiveFamily(@PathVariable(name = "id") final String id, @RequestBody final Family family) {
        try {
            if (family == null || id == null || StringUtils.isBlank(family.get_id()) ||
                    StringUtils.isBlank(family.getFamilyName()) || family.getGuardians() == null || family.getGuardians().isEmpty() ||
                    StringUtils.isBlank(family.get_id()) || family.getStudents() == null || family.getStudents().isEmpty() ||
                    StringUtils.isBlank(String.valueOf(family.isActive()))) {
                logger.error("Error in 'updateActiveFamily': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(family.get_id())) {
                logger.error("Error in 'updateActiveFamily': id parameter does not match id in family");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Family> familyOptional = familyRepository.getFamily(id);
                if (!familyOptional.isPresent()) {
                    logger.error("Error in 'updateActiveFamily': tried to update a family that does not exist");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                } else {
                    // Check for uninvoiced line items
                    List<LineItem> uninvoicedLineItemList = lineItemRepository.getLineItems(familyOptional.get().get_id(), null,
                            null, "null", null, null, null, null);
                    if (familyOptional.get().isActive() && !family.isActive() && !uninvoicedLineItemList.isEmpty()) {
                        logger.error("Error in 'updateActiveFamily': you cannot deactivate a family with uninvoiced line items");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    // Check for unpaid invoices
                    List<Invoice> unpaidInvoiceList = invoiceRepository.getInvoices(familyOptional.get().get_id(), "false");
                    if (familyOptional.get().isActive() && !family.isActive() && !unpaidInvoiceList.isEmpty()) {
                        logger.error("Error in 'updateActiveFamily': you cannot deactivate a family with unpaid invoices");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                    }
                    // Deactivate guardians
                    for (String guardianID : familyOptional.get().getGuardians()) {
                        Optional<Guardian> guardian = guardianRepository.getGuardian(guardianID);
                        if (guardian.isPresent()) {
                            guardian.get().setActive(family.isActive());
                            Guardian result = guardianRepository.updateActive(guardian.get().get_id(), guardian.get());
                            if (result == null) {
                                logger.error("Error in 'updateActiveFamily': error building guardian");
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                            }
                        } else {
                            logger.error("Error in 'updateActiveFamily': guardian is null");
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                        }
                    }
                    // Deactivate students
                    for (String studentID : familyOptional.get().getStudents()) {
                        Optional<Student> student = studentRepository.getStudent(studentID);
                        if (student.isPresent()) {
                            student.get().setActive(family.isActive());
                            Student result = studentRepository.updateActive(student.get().get_id(), student.get());
                            if (result == null) {
                                logger.error("Error in 'deleteFamily': error building student");
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                            }
                        } else {
                            logger.error("Error in 'updateActiveFamily': student is null");
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                        }
                    }
                    Family result = familyRepository.updateActive(id, family);
                    if (result == null) {
                        logger.error("Error in 'updateActiveFamily': error building family");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateActiveFamily', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
