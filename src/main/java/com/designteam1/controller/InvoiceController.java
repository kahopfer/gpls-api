package com.designteam1.controller;

import com.designteam1.model.Invoice;
import com.designteam1.model.Invoices;
import com.designteam1.model.LineItem;
import com.designteam1.repository.InvoiceRepository;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("invoices")
public class InvoiceController {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    public InvoiceController() {

    }

    public InvoiceController(final InvoiceRepository invoiceRepository, final LineItemRepository lineItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.lineItemRepository = lineItemRepository;
    }

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Invoices> getInvoices(@RequestParam(value = "familyID", defaultValue = "", required = false) final String familyID,
                                                @RequestParam(value = "paid", defaultValue = "", required = false) final String paid) {
        try {
            final Invoices invoices = new Invoices();
            final List<Invoice> invoiceList = invoiceRepository.getInvoices(familyID, paid);
            if (invoiceList == null) {
                return ResponseEntity.ok(invoices);
            }
            invoices.setInvoices(invoiceList);
            return ResponseEntity.ok(invoices);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getInvoices', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Invoice> getInvoice(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Invoice> invoice = invoiceRepository.getInvoice(id);
            if (!invoice.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(invoice.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getInvoice', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Invoice> createInvoice(@RequestBody final Invoice invoice) {
        try {
            if (invoice == null || invoice.getInvoiceFromDate() == null || invoice.getInvoiceToDate() == null
                    || StringUtils.isBlank(invoice.getFamilyID()) || StringUtils.isBlank(String.valueOf(invoice.isPaid()))
                    || invoice.getInvoiceDate() == null) {
                logger.error("Error in 'createInvoice': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Calendar c = Calendar.getInstance();
                c.setTime(invoice.getInvoiceFromDate());
                int dayOfWeekFrom = c.get(Calendar.DAY_OF_WEEK);

                c.setTime(invoice.getInvoiceToDate());
                int dayOfWeekTo = c.get(Calendar.DAY_OF_WEEK);

                if (dayOfWeekFrom != 2 || dayOfWeekTo != 6) {
                    logger.error("Error in 'createInvoice': invoice range must be between a Monday and a Friday");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }

                final List<LineItem> lineItemList = lineItemRepository.getLineItems(invoice.getFamilyID(), null,
                        null, null, null, invoice.getInvoiceFromDate(), invoice.getInvoiceToDate());
                if (lineItemList.isEmpty()) {
                    logger.error("Error in 'createInvoice': no line items in given date range");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                // TODO: Price calculation logic here
                List<String> invoiceIDList = new ArrayList<>();
                for (LineItem lineItem: lineItemList) {
                    invoiceIDList.add(lineItem.get_id());
                }
                invoice.setLineItemsID(invoiceIDList);
                Invoice invoice1 = invoiceRepository.createInvoice(invoice);
                if (invoice1 == null || invoice1.get_id() == null) {
                    logger.error("Error in 'createInvoice': error creating invoice");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    for (LineItem lineItem: lineItemList) {
                        lineItem.setInvoiceID(invoice1.get_id());
                        lineItemRepository.updateLineItem(lineItem.get_id(), lineItem);
                    }
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", invoice1.get_id());
                    return new ResponseEntity<Invoice>(null, header, HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createInvoice', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Invoice> updateInvoice(@PathVariable(name = "id") final String id, @RequestBody final Invoice invoice) {
        try {
            if (invoice == null || id == null || invoice.getInvoiceFromDate() == null || invoice.getInvoiceToDate() == null
                    || StringUtils.isBlank(invoice.getFamilyID()) || StringUtils.isBlank(String.valueOf(invoice.isPaid()))
                    || StringUtils.isBlank(invoice.get_id()) || invoice.getLineItemsID() == null || invoice.getLineItemsID().isEmpty()
                    || invoice.getTotalCost() == null || StringUtils.isEmpty(String.valueOf(invoice.getTotalCost()))
                    || invoice.getInvoiceDate() == null) {
                logger.error("Error in 'updateInvoice': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(invoice.get_id())) {
                logger.error("Error in 'updateInvoice': id parameter does not match id in invoice");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<Invoice> invoiceOptional = invoiceRepository.getInvoice(id);
                if (!invoiceOptional.isPresent()) {
                    return this.createInvoice(invoice);
                } else {
                    Invoice result = invoiceRepository.updateInvoice(id, invoice);
                    if (result == null) {
                        logger.error("Error in 'updateInvoice': error building invoice");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateInvoice', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable(name = "id") final String id) {
        try {
            Optional<Invoice> invoice = invoiceRepository.getInvoice(id);
            if (invoice.isPresent()) {
                if (!invoice.get().isPaid()) {
                    logger.error("Error in 'deleteInvoice': cannot delete an unpaid invoice");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                for (String lineItemID: invoice.get().getLineItemsID()) {
                    Optional<LineItem> lineItemToDelete = lineItemRepository.getLineItem(lineItemID);
                    lineItemToDelete.ifPresent(lineItem -> lineItemRepository.deleteLineItem(lineItem));
                }
                Invoice result = invoiceRepository.deleteInvoice(invoice.get());
                if (result == null) {
                    logger.error("Error in 'deleteInvoice': error deleting invoice");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    return ResponseEntity.status(HttpStatus.OK).body(null);
                }
            } else {
                logger.error("Error in 'deleteInvoice': invoice is null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteInvoice', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
