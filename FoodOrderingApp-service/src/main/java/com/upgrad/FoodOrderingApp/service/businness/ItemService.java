package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ItemService {

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private ItemDao itemDao;

    public List<ItemEntity> getItemsByCategoryAndRestaurant(final String restaurantUuid, final String categoryUuid) {
        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(restaurantUuid);

        CategoryEntity categoryEntity = categoryDao.getCategoryById(categoryUuid);
        List<ItemEntity> itemEntities = new ArrayList<>();
        for (ItemEntity restaurantItemEntity : restaurantEntity.getItems()) {
            for (ItemEntity categoryItemEntity : categoryEntity.getItems()) {
                if (restaurantItemEntity.equals(categoryItemEntity)) {
                    itemEntities.add(restaurantItemEntity);
                }
            }
        }
        return itemEntities;
    }

    public List<ItemEntity> getItemsByPopularity(final RestaurantEntity restaurantEntity) {
        List<OrderEntity> ordersEntities = orderDao.getOrdersByRestaurant(restaurantEntity.getUuid());
        System.out.println(ordersEntities);
        Map<String, Integer> itemCountHashMap = new HashMap<>();
        for (OrderEntity orderedEntity : ordersEntities) {
            System.out.println(orderedEntity.getUuid());
            List<OrderItemEntity> orderItemEntities = orderItemDao.getItemsByOrders(orderedEntity.getUuid());
            for (OrderItemEntity orderItemEntity : orderItemEntities) {
                String itemUUID = orderItemEntity.getItem().getUuid();
                Integer count = itemCountHashMap.get(itemUUID);
                itemCountHashMap.put(itemUUID, (count == null) ? 1 : count + 1);
            }
        }

        List<String> listIdKeys = getTopCountMap(itemCountHashMap, 5);
        List<ItemEntity> popularItems = new ArrayList<>();
        for (String id : listIdKeys) {
            popularItems.add(itemDao.getItemById(id));
        }
        return popularItems;
    }

    private List<String> getTopCountMap(final Map<String, Integer> map, final int limit) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue().compareTo(o1.getValue()));
            }
        });

        List<String> sortedItemKeys = new ArrayList<>();
        int index = 1;

        for (Map.Entry<String, Integer> item : list) {
            if (index <= limit) {
                sortedItemKeys.add(item.getKey());
                index++;
            } else {
                return sortedItemKeys;
            }
        }

        return sortedItemKeys;
    }

    public ItemEntity getItemByUuid(final String itemUuid) throws ItemNotFoundException {
        ItemEntity item = itemDao.getItemById(itemUuid);
        if (item == null) {
            throw new ItemNotFoundException("INF-003", "No item by this id exist");
        }
        return item;
    }
}
