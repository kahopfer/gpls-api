package com.designteam1.controller;

import com.designteam1.model.*;
import com.designteam1.repository.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
@RequestMapping("invoices")
public class InvoiceController {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    public InvoiceController() {

    }

    public InvoiceController(final InvoiceRepository invoiceRepository, final LineItemRepository lineItemRepository,
                             final PriceListRepository priceListRepository, final FamilyRepository familyRepository,
                             final StudentRepository studentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.lineItemRepository = lineItemRepository;
        this.priceListRepository = priceListRepository;
        this.familyRepository = familyRepository;
        this.studentRepository = studentRepository;
    }

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @Autowired
    PriceListRepository priceListRepository;

    @Autowired
    FamilyRepository familyRepository;

    @Autowired
    StudentRepository studentRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getInvoices(@RequestParam(value = "familyID", defaultValue = "", required = false) final String familyID,
                                                   @RequestParam(value = "paid", defaultValue = "", required = false) final String paid) {
        try {
            final Invoices invoices = new Invoices();
            final List<Invoice> invoiceList = invoiceRepository.getInvoices(familyID, paid);
            if (invoiceList == null) {
                return new ApiResponse(invoices).send(HttpStatus.OK);
            }
            invoices.setInvoices(invoiceList);
            return new ApiResponse(invoices).send(HttpStatus.OK);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getInvoices', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the invoices");
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getInvoice(@PathVariable(name = "id") final String id) {
        try {
            final Optional<Invoice> invoice = invoiceRepository.getInvoice(id);
            if (!invoice.isPresent()) {
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the invoice you were looking for");
            } else {
                return new ApiResponse(invoice.get()).send(HttpStatus.OK);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getInvoice', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the invoice");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createInvoice(@RequestBody final Invoice invoice) {
        try {
            if (invoice == null || invoice.getInvoiceFromDate() == null || invoice.getInvoiceToDate() == null
                    || StringUtils.isBlank(invoice.getFamilyID()) || StringUtils.isBlank(String.valueOf(invoice.isPaid()))
                    || invoice.getInvoiceDate() == null) {
                logger.error("Error in 'createInvoice': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else {
                Calendar c = Calendar.getInstance();
                c.setTime(invoice.getInvoiceFromDate());
                int dayOfWeekFrom = c.get(Calendar.DAY_OF_WEEK);

                c.setTime(invoice.getInvoiceToDate());
                int dayOfWeekTo = c.get(Calendar.DAY_OF_WEEK);

                if (dayOfWeekFrom != 2 || dayOfWeekTo != 6) {
                    logger.error("Error in 'createInvoice': invoice range must be between a Monday and a Friday");
                    return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Invoice range must be between a Monday and a Friday");
                }

                final List<LineItem> lineItemList = lineItemRepository.getLineItems(invoice.getFamilyID(), null,
                        "notNull", "null", null, invoice.getInvoiceFromDate(), invoice.getInvoiceToDate(), null);
                if (lineItemList.isEmpty()) {
                    logger.error("Error in 'createInvoice': no line items in given date range");
                    return new ApiResponse().send(HttpStatus.BAD_REQUEST, "No line items in given date range");
                }

                BigDecimal totalInvoiceCost = new BigDecimal(0);

                List<String> invoiceIDList = new ArrayList<>();
                List<LineItem> beforeCareFullMorningLineItems = new ArrayList<>();
                List<LineItem> afterCareFullAfternoonLineItems = new ArrayList<>();

                int weekendLineItems = 0;
                int differentDayLineItems = 0;
                int checkInAfterCheckOut = 0;
                int morningAndAfternoonLineItems = 0;
                for (LineItem lineItem : lineItemList) {
                    if (lineItem.getServiceType().equals("Child Care")) {
                        Calendar checkInCalendar = Calendar.getInstance();
                        checkInCalendar.setTime(lineItem.getCheckIn());
                        int dayOfWeekCheckIn = checkInCalendar.get(Calendar.DAY_OF_WEEK);

                        Calendar checkOutCalendar = Calendar.getInstance();
                        checkOutCalendar.setTime(lineItem.getCheckOut());
                        int dayOfWeekCheckOut = checkOutCalendar.get(Calendar.DAY_OF_WEEK);

                        if (dayOfWeekCheckIn == 1 || dayOfWeekCheckIn == 7 || dayOfWeekCheckOut == 1 || dayOfWeekCheckOut == 7) {
                            weekendLineItems++;
                        } else {
                            if (!(DateUtils.isSameDay(lineItem.getCheckIn(), lineItem.getCheckOut()))) {
                                differentDayLineItems++;
                            } else if (!(lineItem.getCheckIn().before(lineItem.getCheckOut()))) {
                                checkInAfterCheckOut++;
                            } else {
                                int checkInTime = (checkInCalendar.get(Calendar.HOUR_OF_DAY) * 10000) + (checkInCalendar.get(Calendar.MINUTE) * 100) + checkInCalendar.get(Calendar.SECOND);
                                int checkOutTime = (checkOutCalendar.get(Calendar.HOUR_OF_DAY) * 10000) + (checkOutCalendar.get(Calendar.MINUTE) * 100) + checkOutCalendar.get(Calendar.SECOND);
                                if (checkInTime >= 120000 || checkOutTime > 120000) {
                                    if (checkInTime < 120000 || checkOutTime <= 120000) {
                                        morningAndAfternoonLineItems++;
                                    }
                                }
                            }
                        }
                    }
                }
                if (weekendLineItems > 0 || differentDayLineItems > 0 || checkInAfterCheckOut > 0 || morningAndAfternoonLineItems > 0) {
                    // First get family
                    Optional<Family> family = familyRepository.getFamily(invoice.getFamilyID());
                    if (family.isPresent()) {
                        String errorMessage = "Could not create the invoice for the " + family.get().getFamilyName() + " family.";
                        if (weekendLineItems > 0) {
                            errorMessage += " Found " + weekendLineItems + " line items on the weekend.";
                        }
                        if (differentDayLineItems > 0) {
                            errorMessage += " Found " + differentDayLineItems + " line items spanning multiple days.";
                        }
                        if (checkInAfterCheckOut > 0) {
                            errorMessage += " Found " + checkInAfterCheckOut + " line items with a sign in time later than the sign out time.";
                        }
                        if (morningAndAfternoonLineItems > 0) {
                            errorMessage += " Found " + morningAndAfternoonLineItems + " line items that were both in the morning and afternoon.";
                        }
                        logger.error("Error in 'createInvoice': " + errorMessage);
                        return new ApiResponse().send(HttpStatus.BAD_REQUEST, errorMessage);
                    } else {
                        logger.error("Error in 'createInvoice': could not find family");
                        return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the family");
                    }
                }

                for (LineItem lineItem : lineItemList) {
                    BigDecimal lineTotalCost = new BigDecimal(0);

                    switch (lineItem.getServiceType()) {
                        case "Child Care":
                            Calendar checkInCalendar = Calendar.getInstance();
                            checkInCalendar.setTime(lineItem.getCheckIn());
                            int dayOfWeekCheckIn = checkInCalendar.get(Calendar.DAY_OF_WEEK);

                            Calendar checkOutCalendar = Calendar.getInstance();
                            checkOutCalendar.setTime(lineItem.getCheckOut());
                            int dayOfWeekCheckOut = checkOutCalendar.get(Calendar.DAY_OF_WEEK);

                            // Check if check in or check out was on the weekend
                            if (dayOfWeekCheckIn == 1 || dayOfWeekCheckIn == 7 || dayOfWeekCheckOut == 1 || dayOfWeekCheckOut == 7) {
                                lineItem.setServiceType("Weekend");
                            } else {
                                // Check if check in and check out are in same day
                                if (DateUtils.isSameDay(lineItem.getCheckIn(), lineItem.getCheckOut()) && lineItem.getCheckIn().before(lineItem.getCheckOut())) {

                                    int checkInTime = (checkInCalendar.get(Calendar.HOUR_OF_DAY) * 10000) + (checkInCalendar.get(Calendar.MINUTE) * 100) + checkInCalendar.get(Calendar.SECOND);
                                    int checkOutTime = (checkOutCalendar.get(Calendar.HOUR_OF_DAY) * 10000) + (checkOutCalendar.get(Calendar.MINUTE) * 100) + checkOutCalendar.get(Calendar.SECOND);

                                    // Check if check in and check out are in the morning
                                    if (checkInTime < 120000 && checkOutTime <= 120000) {
                                        //Before care logic
                                        String beforeCareStart = "06:30:00";
                                        Date beforeCareStartDate = new SimpleDateFormat("HH:mm:ss").parse(beforeCareStart);
                                        Calendar beforeCareStartCalendar = Calendar.getInstance();
                                        beforeCareStartCalendar.setTime(beforeCareStartDate);
                                        beforeCareStartCalendar.set(Calendar.DAY_OF_MONTH, checkInCalendar.get(Calendar.DAY_OF_MONTH));
                                        beforeCareStartCalendar.set(Calendar.MONTH, checkInCalendar.get(Calendar.MONTH));
                                        beforeCareStartCalendar.set(Calendar.YEAR, checkInCalendar.get(Calendar.YEAR));

                                        String beforeCareEnd = "07:55:00";
                                        Date beforeCareEndDate = new SimpleDateFormat("HH:mm:ss").parse(beforeCareEnd);
                                        Calendar beforeCareEndCalendar = Calendar.getInstance();
                                        beforeCareEndCalendar.setTime(beforeCareEndDate);
                                        beforeCareEndCalendar.set(Calendar.DAY_OF_MONTH, checkInCalendar.get(Calendar.DAY_OF_MONTH));
                                        beforeCareEndCalendar.set(Calendar.MONTH, checkInCalendar.get(Calendar.MONTH));
                                        beforeCareEndCalendar.set(Calendar.YEAR, checkInCalendar.get(Calendar.YEAR));

                                        String beforeCareEndBilling = "07:45:00";
                                        Date beforeCareEndBillingDate = new SimpleDateFormat("HH:mm:ss").parse(beforeCareEndBilling);
                                        Calendar beforeCareEndBillingCalendar = Calendar.getInstance();
                                        beforeCareEndBillingCalendar.setTime(beforeCareEndBillingDate);
                                        beforeCareEndBillingCalendar.set(Calendar.DAY_OF_MONTH, checkInCalendar.get(Calendar.DAY_OF_MONTH));
                                        beforeCareEndBillingCalendar.set(Calendar.MONTH, checkInCalendar.get(Calendar.MONTH));
                                        beforeCareEndBillingCalendar.set(Calendar.YEAR, checkInCalendar.get(Calendar.YEAR));

                                        long earlyDropOffMinutes = 0L;
                                        if (checkOutCalendar.getTimeInMillis() < beforeCareStartCalendar.getTimeInMillis()) {
                                            earlyDropOffMinutes = TimeUnit.MILLISECONDS.toMinutes(checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis());
                                            lineItem.setServiceType("Early Drop-off Fee");
                                        } else if (checkInCalendar.getTimeInMillis() < beforeCareStartCalendar.getTimeInMillis()) {
                                            earlyDropOffMinutes = ((beforeCareStartCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis()) / (60 * 1000)) % 60;
                                        }

                                        BigDecimal earlyDropOffMinutesBigDecimal = new BigDecimal(earlyDropOffMinutes);

                                        List<PriceList> earlyInFee = priceListRepository.getPriceLists("Early In Late Out", null);
                                        if (earlyInFee != null && !earlyInFee.isEmpty()) {
                                            lineItem.setEarlyInLateOutFee(earlyInFee.get(0).getItemValue().multiply(earlyDropOffMinutesBigDecimal));
                                            BigDecimal earlyInFeeTotal = earlyInFee.get(0).getItemValue().multiply(earlyDropOffMinutesBigDecimal);
                                            lineTotalCost = lineTotalCost.add(earlyInFeeTotal);
                                        }

                                        long regularBeforeCareMinutes = 0L;
                                        // If the child was checked out before before care ended and after it started
                                        if (checkOutCalendar.getTimeInMillis() < beforeCareEndCalendar.getTimeInMillis() && checkOutCalendar.getTimeInMillis() > beforeCareStartCalendar.getTimeInMillis()) {
                                            // If the child was checked in after before care started and at or before 7:45
                                            if (checkInCalendar.getTimeInMillis() > beforeCareStartCalendar.getTimeInMillis() && checkInCalendar.getTimeInMillis() <= beforeCareEndBillingCalendar.getTimeInMillis()) {
                                                regularBeforeCareMinutes = TimeUnit.MILLISECONDS.toMinutes(checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis());
                                            } else if (checkInCalendar.getTimeInMillis() <= beforeCareStartCalendar.getTimeInMillis()) {
                                                // If the child was checked in at or before before care started
                                                regularBeforeCareMinutes = TimeUnit.MILLISECONDS.toMinutes(checkOutCalendar.getTimeInMillis() - beforeCareStartCalendar.getTimeInMillis());
                                            } else if (checkInCalendar.getTimeInMillis() > beforeCareEndBillingCalendar.getTimeInMillis()) {
                                                lineItem.setServiceType("Before Care After 7:45");
                                            }
                                            // If the child was checked out after before care ended
                                        } else if (checkOutCalendar.getTimeInMillis() >= beforeCareEndCalendar.getTimeInMillis()) {
                                            // If the child was checked in after before care started and before or at 7:45
                                            if (checkInCalendar.getTimeInMillis() > beforeCareStartCalendar.getTimeInMillis() && checkInCalendar.getTimeInMillis() <= beforeCareEndBillingCalendar.getTimeInMillis()) {
                                                regularBeforeCareMinutes = TimeUnit.MILLISECONDS.toMinutes(beforeCareEndCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis());
                                                // If the child was checked in at or before before care started
                                            } else if (checkInCalendar.getTimeInMillis() <= beforeCareStartCalendar.getTimeInMillis()) {
                                                regularBeforeCareMinutes = TimeUnit.MILLISECONDS.toMinutes(beforeCareEndCalendar.getTimeInMillis() - beforeCareStartCalendar.getTimeInMillis());
                                            } else if (checkInCalendar.getTimeInMillis() > beforeCareEndBillingCalendar.getTimeInMillis()) {
                                                lineItem.setServiceType("Before Care After 7:45");
                                            }
                                        }

                                        if (regularBeforeCareMinutes > 0 && regularBeforeCareMinutes <= 60) {
                                            List<PriceList> beforeCareHourOrLess = priceListRepository.getPriceLists("Before Care Hour or Less", null);
                                            if (beforeCareHourOrLess != null && !beforeCareHourOrLess.isEmpty()) {
                                                lineTotalCost = lineTotalCost.add(beforeCareHourOrLess.get(0).getItemValue());
                                                lineItem.setServiceType("Before Care Hour or Less");
                                            }
                                        } else if (regularBeforeCareMinutes > 60) {
                                            List<PriceList> beforeCareFullMorning = priceListRepository.getPriceLists("Before Care Full Morning", null);
                                            if (beforeCareFullMorning != null && !beforeCareFullMorning.isEmpty()) {
                                                lineTotalCost = lineTotalCost.add(beforeCareFullMorning.get(0).getItemValue());
                                                lineItem.setServiceType("Before Care Full Morning");
                                                beforeCareFullMorningLineItems.add(lineItem);
                                            }
                                        }
                                    } else if (checkInTime >= 120000 && checkOutTime > 120000) {
                                        // Check if check in and check out are in the afternoon
                                        String afterCareStart = "15:30:00";
                                        Date afterCareStartDate = new SimpleDateFormat("HH:mm:ss").parse(afterCareStart);
                                        Calendar afterCareStartCalendar = Calendar.getInstance();
                                        afterCareStartCalendar.setTime(afterCareStartDate);
                                        afterCareStartCalendar.set(Calendar.DAY_OF_MONTH, checkInCalendar.get(Calendar.DAY_OF_MONTH));
                                        afterCareStartCalendar.set(Calendar.MONTH, checkInCalendar.get(Calendar.MONTH));
                                        afterCareStartCalendar.set(Calendar.YEAR, checkInCalendar.get(Calendar.YEAR));

                                        String afterCareEnd = "18:00:00";
                                        Date afterCareEndDate = new SimpleDateFormat("HH:mm:ss").parse(afterCareEnd);
                                        Calendar afterCareEndCalendar = Calendar.getInstance();
                                        afterCareEndCalendar.setTime(afterCareEndDate);
                                        afterCareEndCalendar.set(Calendar.DAY_OF_MONTH, checkInCalendar.get(Calendar.DAY_OF_MONTH));
                                        afterCareEndCalendar.set(Calendar.MONTH, checkInCalendar.get(Calendar.MONTH));
                                        afterCareEndCalendar.set(Calendar.YEAR, checkInCalendar.get(Calendar.YEAR));

                                        long latePickUpMinutes = 0L;
                                        if (checkInCalendar.getTimeInMillis() > afterCareEndCalendar.getTimeInMillis()) {
                                            latePickUpMinutes = TimeUnit.MILLISECONDS.toMinutes(checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis());
                                            lineItem.setServiceType("Late Arrival Fee");
                                        } else if (checkOutCalendar.getTimeInMillis() > afterCareEndCalendar.getTimeInMillis()) {
                                            latePickUpMinutes = TimeUnit.MILLISECONDS.toMinutes(checkOutCalendar.getTimeInMillis() - afterCareEndCalendar.getTimeInMillis());
                                        }

                                        BigDecimal latePickUpMinutesBigDecimal = new BigDecimal(latePickUpMinutes);

                                        List<PriceList> latePickupFee = priceListRepository.getPriceLists("Early In Late Out", null);
                                        if (latePickupFee != null && !latePickupFee.isEmpty()) {
                                            lineItem.setEarlyInLateOutFee(latePickupFee.get(0).getItemValue().multiply(latePickUpMinutesBigDecimal));
                                            BigDecimal latePickupFeeTotal = latePickupFee.get(0).getItemValue().multiply(latePickUpMinutesBigDecimal);
                                            lineTotalCost = lineTotalCost.add(latePickupFeeTotal);
                                        }

                                        long regularAfterCareMinutes = 0L;
                                        // If the child was checked out before after care ended and after it started
                                        if (checkOutCalendar.getTimeInMillis() < afterCareEndCalendar.getTimeInMillis() && checkOutCalendar.getTimeInMillis() > afterCareStartCalendar.getTimeInMillis()) {
                                            // If the child was checked in after after care started
                                            if (checkInCalendar.getTimeInMillis() > afterCareStartCalendar.getTimeInMillis()) {
                                                regularAfterCareMinutes = TimeUnit.MILLISECONDS.toMinutes(checkOutCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis());
                                            } else {
                                                // If the child was checked in at or before after care started
                                                regularAfterCareMinutes = TimeUnit.MILLISECONDS.toMinutes(checkOutCalendar.getTimeInMillis() - afterCareStartCalendar.getTimeInMillis());
                                            }
                                            // If the child was checked out after after care ended
                                        } else if (checkOutCalendar.getTimeInMillis() >= afterCareEndCalendar.getTimeInMillis()) {
                                            // If the child was checked in after after care started
                                            if (checkInCalendar.getTimeInMillis() > afterCareStartCalendar.getTimeInMillis()) {
                                                regularAfterCareMinutes = TimeUnit.MILLISECONDS.toMinutes(afterCareEndCalendar.getTimeInMillis() - checkInCalendar.getTimeInMillis());
                                                // If the child was checked in at or before after care started
                                            } else {
                                                regularAfterCareMinutes = TimeUnit.MILLISECONDS.toMinutes(afterCareEndCalendar.getTimeInMillis() - afterCareStartCalendar.getTimeInMillis());
                                            }
                                        } else if (checkInCalendar.getTimeInMillis() < afterCareStartCalendar.getTimeInMillis() && checkOutCalendar.getTimeInMillis() < afterCareStartCalendar.getTimeInMillis()) {
                                            lineItem.setServiceType("After Care Before 3:30");
                                        }

                                        if (regularAfterCareMinutes > 0 && regularAfterCareMinutes <= 60) {
                                            List<PriceList> afterCareHourOrLess = priceListRepository.getPriceLists("After Care Hour or Less", null);
                                            if (afterCareHourOrLess != null && !afterCareHourOrLess.isEmpty()) {
                                                lineTotalCost = lineTotalCost.add(afterCareHourOrLess.get(0).getItemValue());
                                                lineItem.setServiceType("After Care Hour or Less");
                                            }
                                        } else if (regularAfterCareMinutes > 60) {
                                            List<PriceList> afterCareFullMorning = priceListRepository.getPriceLists("After Care Full Afternoon", null);
                                            if (afterCareFullMorning != null && !afterCareFullMorning.isEmpty()) {
                                                lineTotalCost = lineTotalCost.add(afterCareFullMorning.get(0).getItemValue());
                                                lineItem.setServiceType("After Care Full Afternoon");
                                                afterCareFullAfternoonLineItems.add(lineItem);
                                            }
                                        }
                                    } else {
                                        lineItem.setServiceType("Unknown Date Range");
                                    }
                                } else {
                                    lineItem.setServiceType("Unknown Date Range");
                                }
                            }
                            break;
                        case "Annual Registration Fee":
                            List<Student> activeStudents = this.studentRepository.getStudents(lineItem.getFamilyID(), null, "true");
                            if (activeStudents.size() > 1) {
                                List<PriceList> familyRegistrationFee = priceListRepository.getPriceLists("Annual Registration Fee (Family)", null);
                                if (familyRegistrationFee != null && !familyRegistrationFee.isEmpty()) {
                                    lineTotalCost = lineTotalCost.add(familyRegistrationFee.get(0).getItemValue());
                                    lineItem.setServiceType("Annual Registration Fee (Family)");
                                }
                            } else {
                                List<PriceList> familyRegistrationFee = priceListRepository.getPriceLists("Annual Registration Fee (Single)", null);
                                if (familyRegistrationFee != null && !familyRegistrationFee.isEmpty()) {
                                    lineTotalCost = lineTotalCost.add(familyRegistrationFee.get(0).getItemValue());
                                    lineItem.setServiceType("Annual Registration Fee (Single)");
                                }
                            }
                            break;
                        default:
                            List<PriceList> extraItemPrice = priceListRepository.getPriceLists(lineItem.getServiceType(), null);
                            if (extraItemPrice != null && !extraItemPrice.isEmpty()) {
                                lineTotalCost = lineTotalCost.add(extraItemPrice.get(0).getItemValue());
                            }
                            break;
                    }

                    lineItem.setLineTotalCost(lineTotalCost);
                    LineItem result = lineItemRepository.updateLineItem(lineItem.get_id(), lineItem);
                    if (result == null) {
                        logger.error("Error in 'createInvoice': error updating line item");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the line items");
                    } else {
                        totalInvoiceCost = totalInvoiceCost.add(lineItem.getLineTotalCost());
                        invoiceIDList.add(lineItem.get_id());
                    }
                }

                // First get family
                Optional<Family> family = familyRepository.getFamily(invoice.getFamilyID());
                if (family.isPresent()) {
                    // Go through students in family
                    for (String studentID : family.get().getStudents()) {
                        List<Date> beforeCareFullMorningDates = new ArrayList<>();
                        List<Date> afterCareFullAfternoonDates = new ArrayList<>();
                        // Go through before care full morning line items to see if this student had any
                        for (LineItem lineItem : beforeCareFullMorningLineItems) {
                            if (lineItem.getStudentID().equals(studentID)) {
                                beforeCareFullMorningDates.add(lineItem.getCheckIn());
                            }
                        }
                        // Go through after care full afternoon line items to see if this student had any
                        for (LineItem lineItem : afterCareFullAfternoonLineItems) {
                            if (lineItem.getStudentID().equals(studentID)) {
                                afterCareFullAfternoonDates.add(lineItem.getCheckIn());
                            }
                        }
                        // Figure out how many (if any) before care full morning weeks this student had, and create line items for the discounts
                        int numberOfFullMorningWeeks = checkForFullWeekDiscount(beforeCareFullMorningDates);
                        for (int i = 0; i < numberOfFullMorningWeeks; i++) {
                            LineItem fullMorningWeekDiscount = new LineItem();
                            fullMorningWeekDiscount.setFamilyID(invoice.getFamilyID());
                            fullMorningWeekDiscount.setStudentID(studentID);
                            fullMorningWeekDiscount.setExtraItem(false);
                            fullMorningWeekDiscount.setCheckIn(invoice.getInvoiceDate());
                            fullMorningWeekDiscount.setCheckOut(invoice.getInvoiceDate());
                            fullMorningWeekDiscount.setServiceType("Before Care Full Week");
                            fullMorningWeekDiscount.setEarlyInLateOutFee(new BigDecimal(0));
                            fullMorningWeekDiscount.setCheckInBy("Other");
                            fullMorningWeekDiscount.setCheckOutBy("Other");
                            fullMorningWeekDiscount.setNotes("");

                            List<PriceList> extraItemPrice = priceListRepository.getPriceLists(fullMorningWeekDiscount.getServiceType(), null);
                            if (extraItemPrice != null && !extraItemPrice.isEmpty()) {
                                fullMorningWeekDiscount.setLineTotalCost(extraItemPrice.get(0).getItemValue().negate());

                                LineItem lineItem1 = lineItemRepository.createLineItem(fullMorningWeekDiscount);
                                if (lineItem1 == null || lineItem1.get_id() == null) {
                                    logger.error("Error in 'createLineItem': error creating full morning week discount line item");
                                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating a full morning week discount line item");
                                } else {
                                    lineItemList.add(lineItem1);
                                    totalInvoiceCost = totalInvoiceCost.add(lineItem1.getLineTotalCost());
                                    invoiceIDList.add(lineItem1.get_id());
                                }
                            }
                        }
                        // Figure out how many (if any) after care full afternoon weeks this student had, and create line items for the discounts
                        int numberOfFullAfternoonWeeks = checkForFullWeekDiscount(afterCareFullAfternoonDates);
                        for (int i = 0; i < numberOfFullAfternoonWeeks; i++) {
                            LineItem fullAfternoonWeekDiscount = new LineItem();
                            fullAfternoonWeekDiscount.setFamilyID(invoice.getFamilyID());
                            fullAfternoonWeekDiscount.setStudentID(studentID);
                            fullAfternoonWeekDiscount.setExtraItem(false);
                            fullAfternoonWeekDiscount.setCheckIn(invoice.getInvoiceDate());
                            fullAfternoonWeekDiscount.setCheckOut(invoice.getInvoiceDate());
                            fullAfternoonWeekDiscount.setServiceType("After Care Full Week");
                            fullAfternoonWeekDiscount.setEarlyInLateOutFee(new BigDecimal(0));
                            fullAfternoonWeekDiscount.setCheckInBy("Other");
                            fullAfternoonWeekDiscount.setCheckOutBy("Other");
                            fullAfternoonWeekDiscount.setNotes("");

                            List<PriceList> extraItemPrice = priceListRepository.getPriceLists(fullAfternoonWeekDiscount.getServiceType(), null);
                            if (extraItemPrice != null && !extraItemPrice.isEmpty()) {
                                fullAfternoonWeekDiscount.setLineTotalCost(extraItemPrice.get(0).getItemValue().negate());

                                LineItem lineItem1 = lineItemRepository.createLineItem(fullAfternoonWeekDiscount);
                                if (lineItem1 == null || lineItem1.get_id() == null) {
                                    logger.error("Error in 'createLineItem': error creating full afternoon week discount line item");
                                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating a full afternoon week discount line item");
                                } else {
                                    lineItemList.add(lineItem1);
                                    totalInvoiceCost = totalInvoiceCost.add(lineItem1.getLineTotalCost());
                                    invoiceIDList.add(lineItem1.get_id());
                                }
                            }
                        }
                    }
                } else {
                    logger.error("Error in 'createInvoice': could not find family");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the family");
                }

                invoice.setLineItemsID(invoiceIDList);
                invoice.setTotalCost(totalInvoiceCost);
                Invoice invoice1 = invoiceRepository.createInvoice(invoice);
                if (invoice1 == null || invoice1.get_id() == null) {
                    logger.error("Error in 'createInvoice': error creating invoice");
                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the invoice");
                } else {
                    for (LineItem lineItem : lineItemList) {
                        lineItem.setInvoiceID(invoice1.get_id());
                        LineItem result = lineItemRepository.updateLineItem(lineItem.get_id(), lineItem);
                        if (result == null) {
                            logger.error("Error in 'createInvoice': error adding invoice ID to line item");
                            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while adding the invoice ID to a line item");
                        }
                    }
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", invoice1.get_id());
                    return new ApiResponse().send(HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createInvoice', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the invoice");
        }
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updateInvoice(@PathVariable(name = "id") final String id, @RequestBody final Invoice invoice) {
        try {
            if (invoice == null || id == null || invoice.getInvoiceFromDate() == null || invoice.getInvoiceToDate() == null
                    || StringUtils.isBlank(invoice.getFamilyID()) || StringUtils.isBlank(String.valueOf(invoice.isPaid()))
                    || StringUtils.isBlank(invoice.get_id()) || invoice.getLineItemsID() == null || invoice.getLineItemsID().isEmpty()
                    || invoice.getTotalCost() == null || StringUtils.isBlank(String.valueOf(invoice.getTotalCost()))
                    || invoice.getInvoiceDate() == null) {
                logger.error("Error in 'updateInvoice': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (!id.equals(invoice.get_id())) {
                logger.error("Error in 'updateInvoice': id parameter does not match id in invoice");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in invoice");
            } else {
                Optional<Invoice> invoiceOptional = invoiceRepository.getInvoice(id);
                if (!invoiceOptional.isPresent()) {
                    logger.error("Error in 'updateInvoice': could not find invoice to update");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the invoice you were trying to update");
                } else {
                    Invoice result = invoiceRepository.updateInvoice(id, invoice);
                    if (result == null) {
                        logger.error("Error in 'updateInvoice': error building invoice");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the invoice");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updateInvoice', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the invoice");
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<ApiResponse> deleteInvoice(@PathVariable(name = "id") final String id) {
        try {
            Optional<Invoice> invoice = invoiceRepository.getInvoice(id);
            if (invoice.isPresent()) {
                if (!invoice.get().isPaid()) {
                    logger.error("Error in 'deleteInvoice': cannot delete an unpaid invoice");
                    return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot delete an unpaid invoice");
                }
                for (String lineItemID : invoice.get().getLineItemsID()) {
                    Optional<LineItem> lineItemToDelete = lineItemRepository.getLineItem(lineItemID);
                    lineItemToDelete.ifPresent(lineItem -> lineItemRepository.deleteLineItem(lineItem));
                }
                Invoice result = invoiceRepository.deleteInvoice(invoice.get());
                if (result == null) {
                    logger.error("Error in 'deleteInvoice': error deleting invoice");
                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the invoice");
                } else {
                    return new ApiResponse().send(HttpStatus.OK);
                }
            } else {
                logger.error("Error in 'deleteInvoice': invoice is null");
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "An error occurred while deleting the invoice");
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deleteInvoice', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the invoice");
        }
    }

    private int checkForFullWeekDiscount(List<Date> dates) {
        List<Date> trimmedDates = new ArrayList<>();
        for (Date date : dates) {
            trimmedDates.add(trimDate(date));
        }
        List<Date> datesWithoutDuplicates = new ArrayList<>(new HashSet<>(trimmedDates));

        Collections.sort(datesWithoutDuplicates);
        int consecutiveDates = 0;
        int numberOfFullWeeks = 0;
        Date last = null;
        Calendar c = Calendar.getInstance();

        for (int i = 0; i < datesWithoutDuplicates.size(); i++) {
            c.setTime(datesWithoutDuplicates.get(i));
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.add(Calendar.DATE, -1);
            if (c.getTime().equals(last)) {
                consecutiveDates++;
            } else {
                consecutiveDates = 0;
            }
            if (consecutiveDates == 4) {
                consecutiveDates = 0;
                numberOfFullWeeks++;
            }
            Calendar lastCalendar = Calendar.getInstance();
            lastCalendar.setTime(datesWithoutDuplicates.get(i));
            lastCalendar.set(Calendar.HOUR_OF_DAY, 0);
            lastCalendar.set(Calendar.MINUTE, 0);
            lastCalendar.set(Calendar.SECOND, 0);
            lastCalendar.set(Calendar.MILLISECOND, 0);
            last = lastCalendar.getTime();
        }

        return numberOfFullWeeks;
    }

    private Date trimDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        return c.getTime();
    }
}
