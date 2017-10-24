package com.designteam1.controller;

import com.designteam1.model.LineItem;
import com.designteam1.model.LineItems;
import com.designteam1.repository.LineItemRepository;
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
@RequestMapping("lineItems")
public class LineItemController {
    private static final Logger logger = LoggerFactory.getLogger(LineItemController.class);

    public LineItemController() {

    }

    public LineItemController(final LineItemRepository lineItemRepository) {
        this.lineItemRepository = lineItemRepository;
    }

    @Autowired
    LineItemRepository lineItemRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LineItems> getLineItems(@RequestParam(value = "familyID", defaultValue = "", required = false) final String familyID,
                                                  @RequestParam(value = "studentID", defaultValue = "", required = false) final String studentID,
                                                  @RequestParam(value = "checkedOut", defaultValue = "", required = false) final String checkedOut,
                                                  @RequestParam(value = "invoiced", defaultValue = "", required = false) final String invoiced) {
        try {
            final LineItems lineItems = new LineItems();
            final List<LineItem> lineItemList = lineItemRepository.getLineItems(familyID, studentID, checkedOut, invoiced);
            if (lineItemList == null) {
                return ResponseEntity.ok(lineItems);
            }
            lineItems.setLineItems(lineItemList);
            return ResponseEntity.ok(lineItems);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getLineItems', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LineItem> getLineItem(@PathVariable(name = "id") final String id) {
        try {
            final Optional<LineItem> lineItem = lineItemRepository.getLineItem(id);
            if (!lineItem.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(lineItem.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getLineItem', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // extraItems, earlyInLateOutFee, lineTotalCost, notes, and invoiceID are not required
    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LineItem> updateLineItem(@PathVariable(name = "id") final String id, @RequestBody final LineItem lineItem) {
        try {
            if (lineItem == null || id == null || StringUtils.isBlank(lineItem.get_id())
                    || StringUtils.isBlank(lineItem.getFamilyID()) || StringUtils.isBlank(lineItem.getStudentID())
                    || lineItem.getCheckIn() == null || lineItem.getCheckOut() == null
                    || StringUtils.isBlank(lineItem.getCheckInBy()) || StringUtils.isBlank(lineItem.getCheckOutBy())) {
                logger.error("Error in 'updateLineItem': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (lineItem.getCheckIn().after(lineItem.getCheckOut())) {
                logger.error("Error in 'updateLineItem': check in time is later than check out time");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(lineItem.get_id())) {
                logger.error("Error in 'updateLineItem': id parameter does not match id in lineItem");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<LineItem> lineItemOptional = lineItemRepository.getLineItem(id);
                if (!lineItemOptional.isPresent()) {
                    return this.createLineItem(lineItem);
                } else {
                    LineItem result = lineItemRepository.updateLineItem(id, lineItem);
                    if (result == null) {
                        logger.error("Error in 'updateLineItem': error building lineItem");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateLineItem', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // checkOut, checkOutBy, extraItems, earlyInLateOutFee, lineTotalCost, notes, and invoiceID are not required
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LineItem> createLineItem(@RequestBody final LineItem lineItem) {
        try {
            if (lineItem == null || StringUtils.isBlank(lineItem.getFamilyID()) || StringUtils.isBlank(lineItem.getStudentID())
                    || lineItem.getCheckIn() == null || StringUtils.isBlank(lineItem.getCheckInBy())) {
                logger.error("Error in 'createLineItem': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (lineItem.getCheckOut() != null && lineItem.getCheckIn().after(lineItem.getCheckOut())) {
                logger.error("Error in 'updateLineItem': check in time is later than check out time");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                LineItem lineItem1 = lineItemRepository.createLineItem(lineItem);
                if (lineItem1 == null || lineItem1.get_id() == null) {
                    logger.error("Error in 'createLineItem': error creating lineItem");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", lineItem1.get_id());
                    return new ResponseEntity<LineItem>(null, header, HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createLineItem', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteLineItem(@PathVariable(name = "id") final String id) {
        try {
            Optional<LineItem> lineItem = lineItemRepository.getLineItem(id);
            if (lineItem.isPresent()) {
                LineItem result = lineItemRepository.deleteLineItem(lineItem.get());
                if (result == null) {
                    logger.error("Error in 'deleteLineItem': error deleting lineItem");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(null);
                }
            } else {
                logger.error("Error in 'deleteLineItem': lineItem is null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteLineItem', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
