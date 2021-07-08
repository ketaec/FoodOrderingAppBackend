package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.RestaurantService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

@CrossOrigin
@RestController
@RequestMapping("/")
public class RestaurantController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    private ItemService itemService;

    /**
     * Get restaurant list endpoint
     * @return ResponseEntity RestaurantListResponse
     */
    @RequestMapping(
            method = RequestMethod.GET,
            path = "/restaurant",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantList() {
        List<RestaurantEntity> listRestaurantEntity = restaurantService.restaurantsByRating();
        List<RestaurantList> listRestaurantList = getListRestaurantListFromListRestaurantEntity(listRestaurantEntity);

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(listRestaurantList);
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }

    private List<RestaurantList> getListRestaurantListFromListRestaurantEntity(List<RestaurantEntity> listRestaurantEntity) {

        List<RestaurantList> listRestaurantList = new ArrayList<>();

        for (RestaurantEntity restaurantEntity : listRestaurantEntity) {

            List<CategoryEntity> listCategoryEntity = categoryService.getCategoriesByRestaurant(restaurantEntity.getUuid());
            StringBuilder sbCategory = new StringBuilder();
            for (CategoryEntity c : listCategoryEntity) {
                sbCategory.append(c.getCategoryName() + ", ");
            }

            listRestaurantList.add(new RestaurantList().id(UUID.fromString(restaurantEntity.getUuid()))
                    .restaurantName(restaurantEntity.getRestaurantName())
                    .averagePrice(restaurantEntity.getAvgPrice())
                    .categories(sbCategory.substring(0, sbCategory.length() - 2))
                    .address(getRestaurantDetailsResponseAddress(restaurantEntity))
                    .customerRating(BigDecimal.valueOf(restaurantEntity.getCustomerRating()))
                    .numberCustomersRated(restaurantEntity.getNumberCustomersRated())
                    .photoURL(restaurantEntity.getPhotoUrl()));
        }
        return listRestaurantList;
    }

    private RestaurantDetailsResponseAddress getRestaurantDetailsResponseAddress(RestaurantEntity restaurantEntity) {
        return new RestaurantDetailsResponseAddress().id(UUID.fromString(restaurantEntity.getAddress().getUuid()))
                .flatBuildingName(restaurantEntity.getAddress().getFlatBuilNo())
                .locality(restaurantEntity.getAddress().getLocality())
                .city(restaurantEntity.getAddress().getCity())
                .pincode(restaurantEntity.getAddress().getPincode())
                .state(new RestaurantDetailsResponseAddressState()
                        .id(UUID.fromString(restaurantEntity.getAddress().getState().getStateUuid()))
                        .stateName(restaurantEntity.getAddress().getState().getStateName()));
    }

    // get restaurant list by restaurant name
    @RequestMapping(method = RequestMethod.GET,
            path = "/restaurant/name/{restaurant_name}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantListByName(
            @PathVariable("restaurant_name") final String restaurantName)
            throws RestaurantNotFoundException {

        List<RestaurantEntity> listRestaurantEntity = restaurantService.restaurantsByName(restaurantName);
        List<RestaurantList> listRestaurantList = getListRestaurantListFromListRestaurantEntity(listRestaurantEntity);

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(listRestaurantList);
        return new ResponseEntity<RestaurantListResponse>(restaurantListResponse, HttpStatus.OK);
    }

    // get restaurant list by category
    @RequestMapping(method = RequestMethod.GET,
            path = "/restaurant/category/{category_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantListResponse> getRestaurantListByCategory(
            @PathVariable("category_id") final String categoryId)
            throws CategoryNotFoundException {

        List<RestaurantEntity> listRestaurantEntity = restaurantService.restaurantByCategory(categoryId);
        List<RestaurantList> listRestaurantList = getListRestaurantListFromListRestaurantEntity(listRestaurantEntity);

        RestaurantListResponse restaurantListResponse = new RestaurantListResponse().restaurants(listRestaurantList);
        return new ResponseEntity<>(restaurantListResponse, HttpStatus.OK);
    }

    // get restaurant details
    @RequestMapping(method = RequestMethod.GET,
            path = "/api/restaurant/{restaurant_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantDetailsResponse> getRestaurantDetails(
            @PathVariable("restaurant_id") final String restaurantId)
            throws RestaurantNotFoundException {
        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantId);
        List<CategoryList> listCategoryList = getCategoryListByRestaurantId(restaurantId);

        RestaurantDetailsResponse restaurantDetailsResponse = getRestaurantDetailsResponse(restaurantEntity, listCategoryList);
        return new ResponseEntity<RestaurantDetailsResponse>(restaurantDetailsResponse, HttpStatus.OK);
    }

    private List<CategoryList> getCategoryListByRestaurantId(String restaurantId) {
        List<CategoryEntity> listCategory = categoryService.getCategoriesByRestaurant(restaurantId);
        List<CategoryList> listCategoryList = new ArrayList<>();
        for (CategoryEntity c : listCategory) {
            List<ItemEntity> listItemEntity = itemService.getItemsByCategoryAndRestaurant(restaurantId, c.getUuid());
            List<ItemList> listItemList = new ArrayList<>();
            for (ItemEntity i : listItemEntity) {
                listItemList.add(new ItemList()
                        .id(UUID.fromString(i.getUuid()))
                        .itemName(i.getItemName())
                        .itemType(ItemList.ItemTypeEnum.fromValue(i.getType().toString()))
                        .price(i.getPrice()));
            }
            listCategoryList.add(new CategoryList()
                    .id(UUID.fromString(c.getUuid()))
                    .categoryName(c.getCategoryName())
                    .itemList(listItemList));
        }
        return listCategoryList;
    }

    private RestaurantDetailsResponse getRestaurantDetailsResponse(RestaurantEntity restaurantEntity, List<CategoryList> listCategoryList) {
        return new RestaurantDetailsResponse().id(UUID.fromString(restaurantEntity.getUuid()))
                .restaurantName(restaurantEntity.getRestaurantName())
                .averagePrice(restaurantEntity.getAvgPrice())
                .categories(listCategoryList)
                .address(getRestaurantDetailsResponseAddress(restaurantEntity))
                .customerRating(BigDecimal.valueOf(restaurantEntity.getCustomerRating()))
                .numberCustomersRated(restaurantEntity.getNumberCustomersRated())
                .photoURL(restaurantEntity.getPhotoUrl())
                .address(getRestaurantDetailsResponseAddress(restaurantEntity))
                .averagePrice(restaurantEntity.getAvgPrice());
    }

    // update restaurant details
    @RequestMapping(method = RequestMethod.PUT,
            path = "/api/restaurant/{restaurant_id}",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RestaurantUpdatedResponse> updateRestaurantDetails(
            @RequestHeader("authorization") final String authorization,
            @PathVariable("restaurant_id") final String restaurantId,
            @RequestParam(name = "customer_rating", required = true) Double customerRating)
            throws AuthorizationFailedException, RestaurantNotFoundException, InvalidRatingException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        RestaurantEntity restaurantEntity = restaurantService.restaurantByUUID(restaurantId);
        RestaurantEntity updatedRestaurantEntity = restaurantService.updateRestaurantRating(restaurantEntity, customerRating);

        RestaurantUpdatedResponse restaurantUpdatedResponse = new RestaurantUpdatedResponse()
                .id(UUID.fromString(restaurantId)).status("RESTAURANT RATING UPDATED SUCCESSFULLY");

        return new ResponseEntity<RestaurantUpdatedResponse>(restaurantUpdatedResponse, HttpStatus.OK);
    }
}
