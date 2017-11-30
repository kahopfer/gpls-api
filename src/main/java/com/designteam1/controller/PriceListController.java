package com.designteam1.controller;

import com.designteam1.model.ApiResponse;
import com.designteam1.model.LineItem;
import com.designteam1.model.PriceList;
import com.designteam1.model.PriceLists;
import com.designteam1.repository.LineItemRepository;
import com.designteam1.repository.PriceListRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("priceList")
public class PriceListController {
    private static final Logger logger = LoggerFactory.getLogger(PriceListController.class);

    public PriceListController() {

    }

    public PriceListController(final PriceListRepository priceListRepository, final LineItemRepository lineItemRepository) {
        this.priceListRepository = priceListRepository;
        this.lineItemRepository = lineItemRepository;
    }

    @Autowired
    private PriceListRepository priceListRepository;

    @Autowired
    private LineItemRepository lineItemRepository;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getPriceLists(@RequestParam(value = "itemName", defaultValue = "", required = false) final String itemName,
                                                     @RequestParam(value = "itemExtra", defaultValue = "", required = false) final String itemExtra) {
        try {
            final PriceLists priceLists = new PriceLists();
            final List<PriceList> priceListList = priceListRepository.getPriceLists(itemName, itemExtra);
            if (priceListList == null) {
                return new ApiResponse(priceLists).send(HttpStatus.OK);
            }
            priceLists.setPriceLists(priceListList);
            return new ApiResponse(priceLists).send(HttpStatus.OK);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getPriceLists', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the child care rates");
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> getPriceList(@PathVariable(name = "id") final String id) {
        try {
            final Optional<PriceList> priceList = priceListRepository.getPriceList(id);
            if (!priceList.isPresent()) {
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the price list you were looking for");
            } else {
                return new ApiResponse(priceList.get()).send(HttpStatus.OK);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getPriceList', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while getting the child care rate");
        }
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> updatePriceList(@PathVariable(name = "id") final String id, @RequestBody final PriceList priceList) {
        try {
            if (priceList == null || id == null || StringUtils.isBlank(priceList.get_id())
                    || StringUtils.isBlank(priceList.getItemName()) || StringUtils.isBlank(String.valueOf(priceList.getItemValue()))
                    || priceList.getItemValue() == null || StringUtils.isBlank(String.valueOf(priceList.getItemExtra()))) {
                logger.error("Error in 'updatePriceList': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (!id.equals(priceList.get_id())) {
                logger.error("Error in 'updatePriceList': id parameter does not match id in priceList");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "ID parameter does not match ID in child care rate");
            } else if (priceList.getItemValue().compareTo(BigDecimal.ZERO) < 0) {
                logger.error("Error in 'createPriceList': value cannot be less than zero");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Value cannot be less than zero");
            } else {
                Optional<PriceList> priceListOptional = priceListRepository.getPriceList(id);
                if (!priceListOptional.isPresent()) {
                    logger.error("Error in 'createPriceList': could not find price list to update");
                    return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the child care rate you were trying to update");
                } else {
                    PriceList result = priceListRepository.updatePriceList(id, priceList);
                    if (result == null) {
                        logger.error("Error in 'updatePriceList': error building priceList");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the child care rate");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updatePriceList', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while updating the child care rate");
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> createPriceList(@RequestBody final PriceList priceList) {
        try {
            if (priceList == null || StringUtils.isBlank(priceList.getItemName()) || priceList.getItemValue() == null
                    || StringUtils.isBlank(String.valueOf(priceList.getItemValue())) || StringUtils.isBlank(String.valueOf(priceList.getItemExtra()))) {
                logger.error("Error in 'createPriceList': missing required field");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Missing a required field");
            } else if (String.valueOf(priceList.getItemExtra()).equals("false")) {
                logger.error("Error in 'createPriceList': cannot create new non extra items");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot create new non-extra items");
            } else if (priceList.getItemValue().compareTo(BigDecimal.ZERO) < 0 &&
                    String.valueOf(priceList.getItemExtra()).equals("false")) {
                logger.error("Error in 'createPriceList': non-extra item value cannot be less than zero");
                return new ApiResponse().send(HttpStatus.BAD_REQUEST, "A non-extra item value cannot be less than zero");
            } else {
                PriceList priceList1 = priceListRepository.createPriceList(priceList);
                if (priceList1 == null || priceList1.get_id() == null) {
                    logger.error("Error in 'createPriceList': error creating priceList");
                    return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the child care rate");
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", priceList1.get_id());
                    return new ApiResponse().send(HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createPriceList', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the child care rate");
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<ApiResponse> deletePriceList(@PathVariable(name = "id") final String id) {
        try {
            Optional<PriceList> priceList = priceListRepository.getPriceList(id);
            if (priceList.isPresent()) {
                if (String.valueOf(priceList.get().getItemExtra()).equals("false")) {
                    logger.error("Error in 'createPriceList': cannot delete non extra items");
                    return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot delete non-extra items");
                }
                List<LineItem> lineItems = lineItemRepository.getLineItems(null, null,
                        null, "null", priceList.get().getItemName(), null, null, null);
                if (!lineItems.isEmpty()) {
                    logger.error("Error in 'createPriceList': cannot delete rates that are in uninvoiced line items");
                    return new ApiResponse().send(HttpStatus.BAD_REQUEST, "Cannot delete child care rates that are in uninvoiced line items");
                } else {
                    PriceList result = priceListRepository.deletePriceList(priceList.get());
                    if (result == null) {
                        logger.error("Error in 'deletePriceList': error deleting priceList");
                        return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the child care rate");
                    } else {
                        return new ApiResponse().send(HttpStatus.OK);
                    }
                }
            } else {
                logger.error("Error in 'deletePriceList': priceList is null");
                return new ApiResponse().send(HttpStatus.NOT_FOUND, "Could not find the child care rate you were trying to delete");
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deletePriceList', " + e);
            return new ApiResponse().send(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the child care rate");
        }
    }
}
