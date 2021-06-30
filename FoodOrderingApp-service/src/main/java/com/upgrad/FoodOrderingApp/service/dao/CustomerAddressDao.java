package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CustomerAddressDao {

    @PersistenceContext
    private EntityManager entityManager;


    //To save saveCustomerAddress
    public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity){
        entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }

    public List<CustomerAddressEntity> getAllCustomerAddressByCustomer(CustomerEntity customerEntity){
        try{
            Integer active = 1; // if address is active value is 1
            List <CustomerAddressEntity> customerAddressEntities = entityManager.createNamedQuery("getAllCustomerAddressByCustomer",CustomerAddressEntity.class).setParameter("customer_entity",customerEntity).setParameter("active",active).getResultList();
            return customerAddressEntities;
        }catch (NoResultException nre){
            return null;
        }
    }
}
