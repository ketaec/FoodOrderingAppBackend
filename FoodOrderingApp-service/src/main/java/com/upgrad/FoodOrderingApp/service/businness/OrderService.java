package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CouponDao;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private CouponDao couponDao;

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
}
