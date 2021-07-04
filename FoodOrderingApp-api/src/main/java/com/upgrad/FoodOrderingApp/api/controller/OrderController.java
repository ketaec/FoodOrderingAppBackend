package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.CouponDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.OrderListCoupon;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.OrderService;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
public class OrderController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @RequestMapping(method = RequestMethod.GET,
            path = "/order/coupon/{coupon_name}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<OrderListCoupon> couponsByCouponName(
            @RequestHeader("authorization") final String authorization,
            @PathVariable(name = "coupon_name", required = false) String couponName)
            throws AuthorizationFailedException, CouponNotFoundException {
        String accessToken = authorization.split("Bearer ")[1];
        customerService.getCustomer(accessToken);

        final CouponEntity coupon = orderService.getCouponByCouponName(couponName);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse();
        OrderListCoupon orderListCoupon = new OrderListCoupon();
        orderListCoupon.id(UUID.fromString(coupon.getUuid())).couponName(coupon.getCoupon_name())
                .percent(coupon.getPercent());

        return new ResponseEntity<OrderListCoupon>(orderListCoupon, HttpStatus.OK);
    }

}
