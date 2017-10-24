package com.designteam1.repository;

import com.designteam1.model.PriceList;
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
public class PriceListRepositoryMongo implements PriceListRepository {
    
    private final String collectionName = "PriceList";

    @Autowired
    private MongoTemplate mt;
    
    @Override
    public List<PriceList> getPriceLists(String itemName, String itemExtra) {
        Query query = new Query();

        if (StringUtils.isNotEmpty(itemName)) {
            query.addCriteria(Criteria.where("itemName").is(itemName));
        }
        if (StringUtils.isNotEmpty(itemExtra)) {
            Boolean itemExtra1 = Boolean.valueOf(itemExtra);
            query.addCriteria(Criteria.where("itemExtra").is(itemExtra1));
        }
        return mt.find(query, PriceList.class, collectionName);
    }

    @Override
    public Optional<PriceList> getPriceList(String id) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        List<PriceList> priceListList = mt.find(query, PriceList.class, collectionName);
        return priceListList.stream().findFirst();
    }

    @Override
    public PriceList updatePriceList(String id, PriceList priceList) {
        final Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        final Update update = new Update();

        update.set("itemValue", priceList.getItemValue());

        final WriteResult result = mt.updateFirst(query, update, PriceList.class, collectionName);

        if (result != null) {
            return priceList;
        } else {
            return null;
        }
    }

    @Override
    public PriceList createPriceList(PriceList priceList) {
        priceList.set_id(null);
        mt.save(priceList, collectionName);
        return priceList;
    }

    @Override
    public PriceList deletePriceList(PriceList priceList) {
        mt.remove(priceList, collectionName);
        return priceList;
    }
}
