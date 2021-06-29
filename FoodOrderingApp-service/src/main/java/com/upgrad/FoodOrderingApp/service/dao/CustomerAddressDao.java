package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class CustomerAddressDao {

    @PersistenceContext
    private EntityManager entityManager;


    //To save saveCustomerAddress
    public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity){
        entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }
}
