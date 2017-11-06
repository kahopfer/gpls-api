package com.designteam1.repository;

import com.designteam1.model.LineItem;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface LineItemRepository {
    List<LineItem> getLineItems(final String familyID, final String studentID, final String checkedOut,
                                final String invoiced, final String serviceType, final Date fromDate, final Date toDate);

    Optional<LineItem> getLineItem(final String id);

    LineItem updateLineItem(String id, LineItem lineItem);

    LineItem createLineItem(LineItem lineItem);

    LineItem deleteLineItem(LineItem lineItem);
}
