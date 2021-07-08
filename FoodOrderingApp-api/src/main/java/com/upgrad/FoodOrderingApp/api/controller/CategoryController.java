package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.CategoriesListResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryListResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // get all categories
    @RequestMapping(method = RequestMethod.GET,
            path="/category",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoriesListResponse> getAllCategories() {
        List<CategoryEntity> categoryEntityList = categoryService.getAllCategoriesOrderedByName();
        CategoriesListResponse categoriesListResponse = new CategoriesListResponse();
        for(CategoryEntity categoryEntity : categoryEntityList) {
            CategoryListResponse categoryListResponse = new CategoryListResponse()
                    .id(UUID.fromString(categoryEntity.getUuid()))
                    .categoryName(categoryEntity.getCategoryName());
            categoriesListResponse.addCategoriesItem(categoryListResponse);
        }

        return new ResponseEntity<CategoriesListResponse>(categoriesListResponse, HttpStatus.OK );
    }

    // get category details
    @RequestMapping(method = RequestMethod.GET,
            path = "/category/{category_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CategoryDetailsResponse> getCategoryDetails(
            @PathVariable("category_id") String categoryUUID)
            throws CategoryNotFoundException {
        CategoryEntity category = categoryService.getCategoryById(categoryUUID);

        List<ItemEntity> categoryItems = category.getItems();
        List<ItemList> itemsList = new ArrayList<ItemList>();
        if (categoryItems != null && !categoryItems.isEmpty()) {
            categoryItems.forEach(item -> {
                ItemList itemList = new ItemList();
                itemList.id(UUID.fromString(item.getUuid())).itemName(item.getItemName())
                        .price(item.getPrice())
                        .itemType(ItemList.ItemTypeEnum.fromValue(item.getType().getValue()));
                itemsList.add(itemList);
            });
        }

        CategoryDetailsResponse categoryDetailsResponse = new CategoryDetailsResponse();
        categoryDetailsResponse.id(UUID.fromString(category.getUuid()))
                .categoryName(category.getCategoryName()).itemList(itemsList);
        return new ResponseEntity<CategoryDetailsResponse>(categoryDetailsResponse, HttpStatus.OK);
    }
}
