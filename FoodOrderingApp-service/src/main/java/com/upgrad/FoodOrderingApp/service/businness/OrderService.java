package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CouponDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderItemDao;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private CouponDao couponDao;

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    public CouponEntity getCouponByCouponName(final String couponName) throws CouponNotFoundException {
        if (couponName.isEmpty()) {
            throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
        }

        CouponEntity coupon = couponDao.getCouponDetailsByName(couponName);
        if (coupon == null) {
            throw new CouponNotFoundException("CPF-001", "No coupon by this name");
        }
        return coupon;
    }

    public CouponEntity getCouponByCouponId(final String couponId) throws CouponNotFoundException {
        CouponEntity coupon = couponDao.getCouponByCouponId(couponId);
        if (coupon == null) {
            throw new CouponNotFoundException("CPF-001", "No coupon by this name");
        }
        return coupon;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderEntity saveOrder(OrderEntity orderEntity)
            throws CouponNotFoundException, AddressNotFoundException,
            PaymentMethodNotFoundException, RestaurantNotFoundException, AuthorizationFailedException {

        if (orderEntity.getCoupon() == null) {
            throw new CouponNotFoundException("CPF-002", "No coupon by this id");
        }

        if (orderEntity.getAddress() == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        CustomerAddressEntity customerAddressEntity = customerAddressDao.getCustomerAddressByAddress(orderEntity.getAddress());

        if (!customerAddressEntity.getCustomer().equals(orderEntity.getCustomer())) {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

        if (orderEntity.getPayment() == null) {
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        }

        if (orderEntity.getRestaurant() == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }
        return orderDao.saveOrder(orderEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderItemEntity saveOrderItem(OrderItemEntity orderItemEntity) {
        orderItemDao.saveOrderItem(orderItemEntity);
        return orderItemEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<OrderEntity> getOrdersByCustomers(final String customerUuid) {
        return orderDao.getOrdersByCustomer(customerUuid);
    }
}
