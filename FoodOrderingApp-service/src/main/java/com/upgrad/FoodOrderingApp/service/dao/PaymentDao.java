package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class PaymentDao {

    @PersistenceContext
    private EntityManager entityManager;

    public PaymentEntity getPaymentByPaymentId(String paymentId) {
        try{
            PaymentEntity paymentEntity = entityManager.createNamedQuery("getPaymentByUuid",PaymentEntity.class).setParameter("uuid",paymentId).getSingleResult();
            return paymentEntity;
        }catch (NoResultException nre){
            return null;
        }
    }

    public List<PaymentEntity> getAllPaymentMethods(){
        try{
            return this.entityManager.createNamedQuery("getAllPaymentMethods", PaymentEntity.class).getResultList();
        } catch(NoResultException nre){
            return null;
        }
    }
}
