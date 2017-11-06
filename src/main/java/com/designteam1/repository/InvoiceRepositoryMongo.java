package com.designteam1.repository;

import com.designteam1.model.Invoice;
import com.mongodb.WriteResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InvoiceRepositoryMongo implements InvoiceRepository {

    private final String collectionName = "Invoices";

    @Autowired
    private MongoTemplate mt;

    @Override
    public List<Invoice> getInvoices(String familyID, String paid) {
        Query query = new Query();

        if (StringUtils.isNotEmpty(familyID)) {
            query.addCriteria(Criteria.where("familyID").is(familyID));
        }
        if (StringUtils.isNotEmpty(paid)) {
            Boolean isPaid = Boolean.valueOf(paid);
            query.addCriteria(Criteria.where("paid").is(isPaid));
        }
        return mt.find(query, Invoice.class, collectionName);
    }

    @Override
    public Optional<Invoice> getInvoice(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<Invoice> invoiceList = mt.find(query, Invoice.class, collectionName);
        return invoiceList.stream().findFirst();
    }

    @Override
    public Invoice updateInvoice(String id, Invoice invoice) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("paid", invoice.isPaid());

        final WriteResult result = mt.updateFirst(query, update, Invoice.class, collectionName);

        if (result != null) {
            return invoice;
        } else {
            return null;
        }
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        invoice.set_id(null);
        mt.save(invoice, collectionName);
        return invoice;
    }

    @Override
    public Invoice deleteInvoice(Invoice invoice) {
        mt.remove(invoice, collectionName);
        return invoice;
    }
}
