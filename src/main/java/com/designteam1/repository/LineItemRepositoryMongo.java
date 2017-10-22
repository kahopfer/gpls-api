package com.designteam1.repository;

import com.designteam1.model.LineItem;
import com.mongodb.WriteResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class LineItemRepositoryMongo implements LineItemRepository {

    private final String collectionName = "LineItems";

    @Autowired
    private MongoTemplate mt;

    @Override
    public List<LineItem> getLineItems(String familyID, String studentID, String checkedOut) {
        Query query = new Query();

        if (StringUtils.isNotEmpty(familyID)) {
            query.addCriteria(Criteria.where("familyID").is(familyID));
        }
        if (StringUtils.isNotEmpty(studentID)) {
            query.addCriteria(Criteria.where("studentID").is(studentID));
        }
        if (StringUtils.isNotEmpty(checkedOut) && checkedOut.equals("null")) {
            query.addCriteria(Criteria.where("checkOut").is(null));
        }
        if (StringUtils.isNotEmpty(checkedOut) && checkedOut.equals("notNull")) {
            query.addCriteria(Criteria.where("checkOut").ne(null));
        }
        return mt.find(query, LineItem.class, collectionName);
    }

    @Override
    public Optional<LineItem> getLineItem(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<LineItem> lineItemList = mt.find(query, LineItem.class, collectionName);
        return lineItemList.stream().findFirst();
    }

    @Override
    public LineItem updateLineItem(String id, LineItem lineItem) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("checkIn", lineItem.getCheckIn());
        update.set("checkOut", lineItem.getCheckOut());
        update.set("extraItems", lineItem.getExtraItems());
        update.set("earlyInLateOutFee", lineItem.getEarlyInLateOutFee());
        update.set("lineTotalCost", lineItem.getLineTotalCost());
        update.set("checkInBy", lineItem.getCheckInBy());
        update.set("checkOutBy", lineItem.getCheckOutBy());
        update.set("notes", lineItem.getNotes());
        update.set("invoiceID", lineItem.getInvoiceID());

        final WriteResult result = mt.updateFirst(query, update, LineItem.class, collectionName);

        if (result != null) {
            return lineItem;
        } else {
            return null;
        }
    }

    @Override
    public LineItem createLineItem(LineItem lineItem) {
        lineItem.set_id(null);
        mt.save(lineItem, collectionName);
        return lineItem;
    }

    @Override
    public LineItem deleteLineItem(LineItem lineItem) {
        mt.remove(lineItem, collectionName);
        return lineItem;
    }
}