package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantCategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    RestaurantDao restaurantDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public List<RestaurantEntity> restaurantsByRating() {
        return restaurantDao.restaurantsByRating();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<RestaurantEntity> restaurantsByName(String restaurantName) throws RestaurantNotFoundException {
        if (restaurantName == null || restaurantName.isEmpty()) {
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }
        List<RestaurantEntity> listRestaurantEntity = restaurantDao.restaurantsByName(restaurantName.toLowerCase());
        return listRestaurantEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<RestaurantEntity> restaurantByCategory(String categoryId) throws CategoryNotFoundException {
        if (categoryId == null || categoryId.isEmpty()) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        List<RestaurantCategoryEntity> listRestaurantCategoryEntity = restaurantDao.restaurantByCategory(categoryId);
        List<RestaurantEntity> listRestaurantEntity = new ArrayList<>();
        for (RestaurantCategoryEntity rc : listRestaurantCategoryEntity) {
            listRestaurantEntity.add(rc.getRestaurant());
        }
        return listRestaurantEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity restaurantByUUID(final String restaurantId) throws RestaurantNotFoundException {
        if (restaurantId == null) {
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }

        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(restaurantId);
        if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        } else {
            return restaurantEntity;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurantEntity, Double customerRating) throws InvalidRatingException {

        if (customerRating == null || customerRating < 1.0 || customerRating > 5.0) {
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }

//        Double newcustomerRating = (restaurantEntity.getCustomerRating() * restaurantEntity.getNumberCustomersRated() + customerRating)
//                / (restaurantEntity.getNumberCustomersRated() + 1);

        Double newCustomerRating = ((restaurantEntity.getCustomerRating() * restaurantEntity.getNumberCustomersRated()) + customerRating) / (restaurantEntity.getNumberCustomersRated() + 1);
        Double roundedNewcustomerRating = BigDecimal.valueOf(newCustomerRating)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        restaurantEntity.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated() + 1);
        restaurantEntity.setCustomerRating(roundedNewcustomerRating);
        RestaurantEntity responseEntity = restaurantDao.updateRestaurant(restaurantEntity);

        return responseEntity;
    }
}
