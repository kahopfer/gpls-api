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

    public FamilyController(final FamilyRepository familyRepository) {
        this.familyRepository = familyRepository;
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
            final List<Family> familyList = familyRepository.getFamilies();
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Family> createFamily(@RequestBody final Family family) {
        try {
            if (family == null || StringUtils.isBlank(family.getFamilyName()) || family.getGuardians() == null || family.getGuardians().isEmpty() ||
                    StringUtils.isBlank(family.get_id()) || family.getStudents() == null || family.getStudents().isEmpty()) {
                logger.error("Error in 'createFamily': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
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
                    StringUtils.isBlank(family.get_id()) || family.getStudents() == null || family.getStudents().isEmpty()) {
                logger.error("Error in 'updateFamily': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(family.get_id())) {
                logger.error("Error in 'updateFamily': id parameter does not match id in family");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Family> familyOptional = familyRepository.getFamily(id);
                if (!familyOptional.isPresent()) {
                    return this.createFamily(family);
                } else {
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
                // Check for uninvoiced line items and unpaid invoices
                List<Invoice> unpaidInvoiceList = invoiceRepository.getInvoices(family.get().get_id(), "false");
                List<LineItem> uninvoicedLineItemList = lineItemRepository.getLineItems(family.get().get_id(), null,
                        null, "null", null, null, null);
                if (!uninvoicedLineItemList.isEmpty()) {
                    logger.error("Error in 'deleteFamily': you cannot delete a family with uninvoiced line items");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                if (!unpaidInvoiceList.isEmpty()) {
                    logger.error("Error in 'deleteFamily': you cannot delete a family with unpaid invoices");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }

                // Delete students
                for (String studentID : family.get().getStudents()) {
                    Optional<Student> student = studentRepository.getStudent(studentID);
                    student.ifPresent(student1 -> studentRepository.deleteStudent(student1));
                }
                // Delete guardians
                for (String guardianID : family.get().getGuardians()) {
                    Optional<Guardian> guardian = guardianRepository.getGuardian(guardianID);
                    guardian.ifPresent(guardian1 -> guardianRepository.deleteGuardian(guardian1));
                }
                // Delete invoices
                List<Invoice> invoiceList = invoiceRepository.getInvoices(family.get().get_id(), null);
                for (Invoice invoice : invoiceList) {
                    if (invoice.isPaid()) {
                        invoiceRepository.deleteInvoice(invoice);
                    }
                }
                // Delete line items
                List<LineItem> lineItemList = lineItemRepository.getLineItems(family.get().get_id(), null,
                        null, null, null, null, null);
                for (LineItem lineItem : lineItemList) {
                    lineItemRepository.deleteLineItem(lineItem);
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
}
