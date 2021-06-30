package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class AddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AddressEntity saveAddress(final AddressEntity address) {
        entityManager.persist(address);
        return address;
    }

    public AddressEntity getAddressByUuid(String uuid){
        try{
            AddressEntity addressEntity = entityManager.createNamedQuery("getAddressByUuid",AddressEntity.class).setParameter("uuid",uuid).getSingleResult();
            return addressEntity;
        }catch (NoResultException nre){
            return null;
        }
    }

    public AddressEntity deleteAddress(AddressEntity addressEntity) {
        entityManager.remove(addressEntity);
        return addressEntity;
    }
}
