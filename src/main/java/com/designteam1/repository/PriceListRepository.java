package com.designteam1.repository;

import com.designteam1.model.PriceList;

import java.util.List;
import java.util.Optional;

public interface PriceListRepository {
    List<PriceList> getPriceLists(final String itemName, final String itemExtra);

    Optional<PriceList> getPriceList(final String id);

    PriceList updatePriceList(String id, PriceList lineItem);

    PriceList createPriceList(PriceList lineItem);

    PriceList deletePriceList(PriceList lineItem);
}
