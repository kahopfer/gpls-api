package com.designteam1.controller;

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
    public ResponseEntity<PriceLists> getPriceLists(@RequestParam(value = "itemName", defaultValue = "", required = false) final String itemName,
                                                    @RequestParam(value = "itemExtra", defaultValue = "", required = false) final String itemExtra) {
        try {
            final PriceLists priceLists = new PriceLists();
            final List<PriceList> priceListList = priceListRepository.getPriceLists(itemName, itemExtra);
            if (priceListList == null) {
                return ResponseEntity.ok(priceLists);
            }
            priceLists.setPriceLists(priceListList);
            return ResponseEntity.ok(priceLists);
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getPriceLists', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriceList> getPriceList(@PathVariable(name = "id") final String id) {
        try {
            final Optional<PriceList> priceList = priceListRepository.getPriceList(id);
            if (!priceList.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            } else {
                return ResponseEntity.ok(priceList.get());
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'getPriceList', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriceList> updatePriceList(@PathVariable(name = "id") final String id, @RequestBody final PriceList priceList) {
        try {
            if (priceList == null || id == null || StringUtils.isBlank(priceList.get_id())
                    || StringUtils.isBlank(priceList.getItemName()) || StringUtils.isBlank(String.valueOf(priceList.getItemValue()))
                    || priceList.getItemValue() == null || StringUtils.isBlank(String.valueOf(priceList.getItemExtra()))) {
                logger.error("Error in 'updatePriceList': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (!id.equals(priceList.get_id())) {
                logger.error("Error in 'updatePriceList': id parameter does not match id in priceList");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (priceList.getItemValue().compareTo(BigDecimal.ZERO) < 0) {
                logger.error("Error in 'createPriceList': value cannot be less than zero");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                Optional<PriceList> priceListOptional = priceListRepository.getPriceList(id);
                if (!priceListOptional.isPresent()) {
                    return this.createPriceList(priceList);
                } else {
                    PriceList result = priceListRepository.updatePriceList(id, priceList);
                    if (result == null) {
                        logger.error("Error in 'updatePriceList': error building priceList");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'updatePriceList', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PriceList> createPriceList(@RequestBody final PriceList priceList) {
        try {
            if (priceList == null || StringUtils.isBlank(priceList.getItemName()) || priceList.getItemValue() == null
                    || StringUtils.isBlank(String.valueOf(priceList.getItemValue())) || StringUtils.isBlank(String.valueOf(priceList.getItemExtra()))) {
                logger.error("Error in 'createPriceList': missing required field");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (String.valueOf(priceList.getItemExtra()).equals("false")) {
                logger.error("Error in 'createPriceList': cannot create new non extra items");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else if (priceList.getItemValue().compareTo(BigDecimal.ZERO) < 0 &&
                    String.valueOf(priceList.getItemExtra()).equals("false")) {
                logger.error("Error in 'createPriceList': non-extra item value cannot be less than zero");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            } else {
                PriceList priceList1 = priceListRepository.createPriceList(priceList);
                if (priceList1 == null || priceList1.get_id() == null) {
                    logger.error("Error in 'createPriceList': error creating priceList");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                } else {
                    HttpHeaders header = new HttpHeaders();
                    header.add("location", priceList1.get_id());
                    return new ResponseEntity<PriceList>(null, header, HttpStatus.CREATED);
                }
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'createPriceList', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> deletePriceList(@PathVariable(name = "id") final String id) {
        try {
            Optional<PriceList> priceList = priceListRepository.getPriceList(id);
            if (priceList.isPresent()) {
                if (String.valueOf(priceList.get().getItemExtra()).equals("false")) {
                    logger.error("Error in 'createPriceList': cannot delete non extra items");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }
                List<LineItem> lineItems = lineItemRepository.getLineItems(null, null,
                        null, "null", priceList.get().getItemName(), null, null, null);
                if (!lineItems.isEmpty()) {
                    logger.error("Error in 'createPriceList': cannot delete rates that are in uninvoiced line items");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                } else {
                    PriceList result = priceListRepository.deletePriceList(priceList.get());
                    if (result == null) {
                        logger.error("Error in 'deletePriceList': error deleting priceList");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(null);
                    }
                }
            } else {
                logger.error("Error in 'deletePriceList': priceList is null");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (final Exception e) {
            logger.error("Caught " + e + " in 'deletePriceList', " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
