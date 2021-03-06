package com.designteam1.controller;

import com.designteam1.model.ApiResponse;
import com.designteam1.model.LineItem;
import com.designteam1.model.LineItems;
import com.designteam1.model.Student;
import com.designteam1.repository.LineItemRepository;
import com.designteam1.repository.StudentRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("lineItems")
public class LineItemController {
    private static final Logger logger = LoggerFactory.getLogger(LineItemController.class);

    public LineItemController() {

    }

    public LineItemController(final LineItemRepository lineItemRepository, final StudentRepository studentRepository) {
        this.lineItemRepository = lineItemRepository;
        this.studentRepository = studentRepository;
    }

    @Autowired
    LineItemRepository lineItemRepository;

    @Autowired
    StudentRepository studentRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getLineItems(@RequestParam(value = "familyID", defaultValue = "", required = false) final String familyID,
                                                    @RequestParam(value = "studentID", defaultValue = "", required = false) final String studentID,
                                                    @RequestParam(value = "checkedOut", defaultValue = "", required = false) final String checkedOut,
                                                    @RequestParam(value = "invoiced", defaultValue = "", required = false) final String invoiced,
                                                    @RequestParam(value = "serviceType", defaultValue = "", required = false) final String serviceType,
                                                    @RequestParam(value = "invoiceID", defaultValue = "", required = false) final String invoiceID,
                                                    @RequestParam(value = "fromDate", required = false, defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDate fromDate,
                                                    @RequestParam(value = "toDate", required = false, defaultValue = "") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDate toDate) {
        try {
            Date fromDate1 = null;
            Date toDate1 = null;
            if (fromDate != null) {
                fromDate1 = java.sql.Date.valueOf(fromDate);
            }
            if (toDate != null) {
                toDate1 = java.sql.Date.valueOf(toDate);
            }
            final LineItems lineItems = new LineItems();
            final List<LineItem> lineItemList = lineItemRepository.getLineItems(familyID, studentID, checkedOut, invoiced,
                    serviceType, fromDate1, toDate1, invoiceID);
            if (lineItemList == null) {
                return new ApiResponse(lineItems).send(HttpStatus.OK);
            }
            lineItems.setLineItems(lineItemList);
            return new ApiResponse(lineItems).send(HttpStatus.OK);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getLineItems', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the line items");
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getLineItem(@PathVariable(name = "id") final String id) {
        try {
            final Optional<LineItem> lineItem = lineItemRepository.getLineItem(id);
            if (!lineItem.isPresent()) {
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the line item you were looking for");
            } else {
                return new ApiResponse(lineItem.get()).send(HttpStatus.OK);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getLineItem', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the line item");
        }
    }

    // notes and invoiceID are not required
    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateLineItem(@PathVariable(name = "id") final String id, @RequestBody final LineItem lineItem) {
        try {
            if (lineItem == null || id == null || StringUtils.isBlank(lineItem.get_id())
                    || StringUtils.isBlank(lineItem.getFamilyID()) || StringUtils.isBlank(lineItem.getStudentID()) || StringUtils.isBlank(String.valueOf(lineItem.isExtraItem()))
                    || lineItem.getCheckIn() == null || lineItem.getCheckOut() == null || StringUtils.isBlank(lineItem.getServiceType())
                    || StringUtils.isBlank(lineItem.getCheckInBy()) || StringUtils.isBlank(lineItem.getCheckOutBy())
                    || StringUtils.isBlank(String.valueOf(lineItem.getLineTotalCost())) || StringUtils.isBlank(String.valueOf(lineItem.getEarlyInLateOutFee()))) {
                logger.error("Error in 'updateLineItem': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (lineItem.getCheckIn().after(lineItem.getCheckOut())) {
                logger.error("Error in 'updateLineItem': check in time is later than check out time");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Check in time is later than check out time");
            } else if (!id.equals(lineItem.get_id())) {
                logger.error("Error in 'updateLineItem': id parameter does not match id in lineItem");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in line item");
            } else {
                List<LineItem> currentLineItems = lineItemRepository.getLineItems(lineItem.getFamilyID(), lineItem.getStudentID(), null, "null", "Child Care", null, null, null);
                if (currentLineItems != null && !currentLineItems.isEmpty()) {
                    for (LineItem lineItem1 : currentLineItems) {
                        if (!(lineItem.get_id().equals(lineItem1.get_id()))) {
                            if (lineItem.getServiceType().equals("Child Care") && isOverlapping(lineItem.getCheckIn(), lineItem.getCheckOut(), lineItem1.getCheckIn(), lineItem1.getCheckOut())) {
                                logger.error("Error in 'updateLineItem': time is overlapping with existing line item");
                                return new ApiResponse().send(HttpStatus.CONFLICT, "Time is overlapping with existing line item");
                            }
                        }
                    }
                }
                if (lineItem.getServiceType().equals("Annual Registration Fee")) {
                    List<LineItem> annualRegistrationFees = lineItemRepository.getLineItems(lineItem.getFamilyID(), null, null, "null", "Annual Registration Fee", null, null, null);
                    if (annualRegistrationFees.size() > 0) {
                        logger.error("Error in 'updateLineItem': cannot have more than one annual registration fee in an invoice");
                        return new ApiResponse().send(HttpStatus.CONFLICT, "Cannot have more than one annual registration fee in an invoice");
                    }
                }
                Optional<LineItem> lineItemOptional = lineItemRepository.getLineItem(id);
                if (!lineItemOptional.isPresent()) {
                    logger.error("Error in 'updateLineItem': could not find line item to update");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the line item you were trying to update");
                } else {
                    if (lineItemOptional.get().getCheckOut() == null) {
                        Optional<Student> studentOptional = studentRepository.getStudent(lineItem.getStudentID());
                        if (!studentOptional.isPresent()) {
                            logger.error("Error in 'updateLineItem': tried to check out a student that does not exist");
                            return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the student you were trying to sign out");
                        } else {
                            if (!studentOptional.get().isActive()) {
                                logger.error("Error in 'updateLineItem': cannot check out an inactive student");
                                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot sign out an inactive student");
                            }
                            if (!studentOptional.get().isCheckedIn()) {
                                logger.error("Error in 'createLineItem': student is already checked out");
                                return new ApiResponse().send(HttpStatus.BAD_REQUEST, studentOptional.get().getFname() + " " + studentOptional.get().getLname() + " is already signed out");
                            }
                            studentOptional.get().setCheckedIn(false);
                            Student result = studentRepository.updateCheckedIn(lineItem.getStudentID(), studentOptional.get());
                            if (result == null) {
                                logger.error("Error in 'updateLineItem': error building student");
                                return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while signing out the student");
                            }
                        }
                    }
                    LineItem result = lineItemRepository.updateLineItem(id, lineItem);
                    if (result == null) {
                        logger.error("Error in 'updateLineItem': error building lineItem");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the line item");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateLineItem', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the line item");
        }
    }

    // checkOut, checkOutBy, notes, and invoiceID are not required
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createLineItem(@RequestBody final LineItem lineItem) {
        try {
            if (lineItem == null || StringUtils.isBlank(lineItem.getFamilyID()) || StringUtils.isBlank(lineItem.getStudentID())
                    || StringUtils.isBlank(String.valueOf(lineItem.isExtraItem())) || lineItem.getCheckIn() == null || StringUtils.isBlank(lineItem.getCheckInBy())
                    || (lineItem.isExtraItem() && StringUtils.isBlank(lineItem.getServiceType())) || (lineItem.getCheckOut() != null && StringUtils.isBlank(lineItem.getCheckOutBy()))
                    || (StringUtils.isNotBlank(lineItem.getCheckOutBy()) && lineItem.getCheckOut() == null) || (lineItem.getCheckOut() != null && StringUtils.isBlank(lineItem.getServiceType()))
                    || StringUtils.isBlank(String.valueOf(lineItem.getLineTotalCost())) || StringUtils.isBlank(String.valueOf(lineItem.getEarlyInLateOutFee())) || StringUtils.isBlank(lineItem.getServiceType())) {
                logger.error("Error in 'createLineItem': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (lineItem.getCheckOut() != null && lineItem.getCheckIn().after(lineItem.getCheckOut())) {
                logger.error("Error in 'createLineItem': check in time is later than check out time");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Check in time is later than check out time");
            } else {
                List<LineItem> currentLineItems = lineItemRepository.getLineItems(lineItem.getFamilyID(), lineItem.getStudentID(), null, "null", "Child Care", null, null, null);
                if (currentLineItems != null && !currentLineItems.isEmpty()) {
                    for (LineItem lineItem1 : currentLineItems) {
                        if (lineItem.getServiceType().equals("Child Care") && isOverlapping(lineItem.getCheckIn(), lineItem.getCheckOut(), lineItem1.getCheckIn(), lineItem1.getCheckOut())) {
                            logger.error("Error in 'createLineItem': time is overlapping with existing line item");
                            return new ApiResponse().send(HttpStatus.CONFLICT, "Time is overlapping with existing line item");
                        }
                    }
                }
                if (lineItem.getServiceType() != null && lineItem.getServiceType().equals("Annual Registration Fee")) {
                    List<LineItem> annualRegistrationFees = lineItemRepository.getLineItems(lineItem.getFamilyID(), null, null, "null", "Annual Registration Fee", null, null, null);
                    if (annualRegistrationFees.size() > 0) {
                        logger.error("Error in 'createLineItem': cannot have more than one annual registration fee in an invoice");
                        return new ApiResponse().send(HttpStatus.CONFLICT, "Cannot have more than one annual registration fee in an invoice");
                    }
                }
                if (lineItem.getCheckOut() == null) {
                    Optional<Student> studentOptional = studentRepository.getStudent(lineItem.getStudentID());
                    if (!studentOptional.isPresent()) {
                        logger.error("Error in 'createLineItem': tried to check in/out a student that does not exist");
                        return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the student you were trying to sign in");
                    } else {
                        if (!studentOptional.get().isActive()) {
                            logger.error("Error in 'createLineItem': cannot check in an inactive student");
                            return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot sign in an inactive student");
                        }
                        if (studentOptional.get().isCheckedIn()) {
                            logger.error("Error in 'createLineItem': student is already checked in");
                            return new ApiResponse().send(HttpStatus.BAD_REQUEST, studentOptional.get().getFname() + " " + studentOptional.get().getLname() + " is already signed in.");
                        }
                        studentOptional.get().setCheckedIn(true);
                        Student result = studentRepository.updateCheckedIn(lineItem.getStudentID(), studentOptional.get());
                        if (result == null) {
                            logger.error("Error in 'createLineItem': error building student");
                            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while signing in the student");
                        }
                    }
                }
                LineItem lineItem1 = lineItemRepository.createLineItem(lineItem);
                if (lineItem1 == null || lineItem1.get_id() == null) {
                    logger.error("Error in 'createLineItem': error creating lineItem");
                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the line item");
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", lineItem1.get_id());
                    return new ApiResponse().send(HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createLineItem', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the line item");
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<ApiResponse> deleteLineItem(@PathVariable(name = "id") final String id) {
        try {
            Optional<LineItem> lineItem = lineItemRepository.getLineItem(id);
            if (lineItem.isPresent()) {
                if (StringUtils.isNotEmpty(lineItem.get().getInvoiceID())) {
                    logger.error("Error in 'deleteLineItem': cannot delete an invoiced line item");
                    return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot delete an invoiced line item");
                } else {
                    LineItem result = lineItemRepository.deleteLineItem(lineItem.get());
                    if (result == null) {
                        logger.error("Error in 'deleteLineItem': error deleting lineItem");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the line item");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            } else {
                logger.error("Error in 'deleteLineItem': lineItem is null");
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the line item you were trying to delete");
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteLineItem', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the line item");
        }
    }

    private static boolean isOverlapping(Date start1, Date end1, Date start2, Date end2) {
        // If the student is currently signed in, they will not have a sign out time yet
        if (end2 == null) {
            return start1.after(start2) || end1.after(start2);
        }
        if (end1 == null) {
            return start2.after(start1) || end2.after(start1);
        }
        return start1.before(end2) && start2.before(end1);
    }
}
