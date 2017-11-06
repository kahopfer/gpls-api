package com.designteam1.repository;

import com.designteam1.model.Invoice;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository {
    List<Invoice> getInvoices(final String familyID, final String paid);

    Optional<Invoice> getInvoice(final String id);

    Invoice updateInvoice(String id, Invoice invoice);

    Invoice createInvoice(Invoice invoice);

    Invoice deleteInvoice(Invoice invoice);
}
