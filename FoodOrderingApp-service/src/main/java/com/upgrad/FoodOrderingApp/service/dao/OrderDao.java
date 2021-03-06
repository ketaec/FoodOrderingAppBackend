package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class OrderDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<OrderEntity> getOrdersByRestaurant(final String  restaurantId){
        try{
            List<OrderEntity> orderEntities = entityManager.createNamedQuery("getAllOrdersByRestaurantUUid",OrderEntity.class).setParameter("restaurantUuid",restaurantId).getResultList();
            return orderEntities;
        }catch (NoResultException nre){
            return null;
        }
    }

    public OrderEntity saveOrder(OrderEntity orderEntity){
        entityManager.persist(orderEntity);
        return orderEntity;
    }

    public List<OrderEntity> getOrdersByCustomer(final String  uuid) {
        try {
            List<OrderEntity> orderEntities = entityManager.createNamedQuery("getAllOrdersOfCustomerByUuid",OrderEntity.class).setParameter("customerUuid",uuid).getResultList();
            return orderEntities;
        }catch (NoResultException nre){
            return null;
        }
    }
}
