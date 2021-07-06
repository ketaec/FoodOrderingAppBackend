package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CrossOrigin
@RestController
public class OrderController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ItemService itemService;

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

    @RequestMapping(method = RequestMethod.POST,
            path = "/order",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(
            @RequestHeader("authorization") final String authorization,
            @RequestBody final SaveOrderRequest saveOrderRequest)
            throws AuthorizationFailedException, AddressNotFoundException, CouponNotFoundException,
            PaymentMethodNotFoundException, RestaurantNotFoundException, ItemNotFoundException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customer = customerService.getCustomer(accessToken);

        String addressUuid = saveOrderRequest.getAddressId();
        String paymentUuid = saveOrderRequest.getPaymentId().toString();
        Double bill = saveOrderRequest.getBill().doubleValue();
        Double discount = saveOrderRequest.getDiscount().doubleValue();
        String couponUuid = saveOrderRequest.getCouponId().toString();
        String restaurantUuid = saveOrderRequest.getRestaurantId().toString();

        CouponEntity coupon = orderService.getCouponByCouponId(couponUuid);
        PaymentEntity payment = paymentService.getPaymentByUUID(paymentUuid);
        AddressEntity address = addressService.getAddressByUUID(addressUuid, customer);
        RestaurantEntity restaurant = restaurantService.restaurantByUUID(restaurantUuid);

        OrderEntity order = new OrderEntity();
        order.setBill(bill);
        order.setCoupon(coupon);
        order.setDiscount(discount);
        order.setDate(new Date());
        order.setPayment(payment);
        order.setCustomer(customer);
        order.setAddress(address);
        order.setRestaurant(restaurant);
        order.setUuid(UUID.randomUUID().toString());

        OrderEntity savedOrder = orderService.saveOrder(order);

        for (ItemQuantity itemQuantity : saveOrderRequest.getItemQuantities()) {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrder(order);
            ItemEntity item = itemService.getItemByUuid(itemQuantity.getItemId().toString());
            orderItemEntity.setItem(item);
            orderItemEntity.setQuantity(itemQuantity.getQuantity());
            orderItemEntity.setPrice(itemQuantity.getPrice());

            orderService.saveOrderItem(orderItemEntity);
        }

        final SaveOrderResponse saveOrderResponse = new SaveOrderResponse();
        saveOrderResponse.setId(savedOrder.getUuid());
        saveOrderResponse.status("ORDER SUCCESSFULLY PLACED");
        return new ResponseEntity<>(saveOrderResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET,
            path = "/order",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getPreviousOrders(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        String accessToken = authorization.split("Bearer ")[1];

        final CustomerEntity customer = customerService.getCustomer(accessToken);
        final List<OrderList> orderList = orderService.getOrdersByCustomers(customer.getUuid())
                .stream()
                .sorted(Comparator.comparing(OrderEntity::getDate)) //Reorder based on date of order creation
                .flatMap((Function<OrderEntity, Stream<OrderList>>) ordersEntity -> Stream.of(prepareOrderListObject(ordersEntity)))
                .collect(Collectors.toList());


        //Return the response payload
        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();
        customerOrderResponse.orders(orderList);
        return new ResponseEntity<>(customerOrderResponse, HttpStatus.OK);
    }

    private OrderList prepareOrderListObject(final OrderEntity orderEntity) {
        final OrderList orderList = new OrderList();
        orderList.id(UUID.fromString(orderEntity.getUuid()));
        orderList.bill( BigDecimal.valueOf(orderEntity.getBill()));
        orderList.discount( BigDecimal.valueOf(orderEntity.getDiscount()));
        orderList.date(orderEntity.getDate().toString());

        orderList.coupon(prepareOrderListCoupon(orderEntity));
        orderList.customer(prepareOrderListCustomer(orderEntity.getCustomer()));
        orderList.address(prepareOrderListAddress(orderEntity.getAddress()));

        return orderList;
    }


    private OrderListAddress prepareOrderListAddress(final AddressEntity address) {
        final OrderListAddress orderListAddress = new OrderListAddress();
        orderListAddress.city(address.getCity());
        orderListAddress.flatBuildingName(address.getFlatBuilNo());
        orderListAddress.id(UUID.fromString(address.getUuid()));
        orderListAddress.locality(address.getLocality());
        orderListAddress.pincode(address.getPincode());
        orderListAddress.setState(prepareOrderListAddressState(address.getState()));
        return orderListAddress;
    }


    private OrderListAddressState prepareOrderListAddressState(final StateEntity state) {
        OrderListAddressState orderListAddressState = new OrderListAddressState();
        orderListAddressState.setStateName(state.getStateName());
        orderListAddressState.setId(UUID.fromString(state.getUuid()));
        return orderListAddressState;
    }


    private OrderListCustomer prepareOrderListCustomer(final CustomerEntity customer) {
        final OrderListCustomer orderListCustomer = new OrderListCustomer();
        orderListCustomer.setId(UUID.fromString(customer.getUuid()));
        orderListCustomer.setFirstName(customer.getFirstName());
        orderListCustomer.setLastName(customer.getLastName());
        orderListCustomer.setContactNumber(customer.getContactNumber());
        orderListCustomer.setEmailAddress(customer.getEmail());
        return orderListCustomer;
    }


    private OrderListCoupon prepareOrderListCoupon(final OrderEntity orderEntity) {
        final CouponEntity couponEntity = orderEntity.getCoupon();
        OrderListCoupon orderListCoupon = new OrderListCoupon();
        orderListCoupon.setCouponName(couponEntity.getCoupon_name());
        orderListCoupon.setId(UUID.fromString(couponEntity.getUuid()));
        orderListCoupon.setPercent(couponEntity.getPercent());
        return orderListCoupon;
    }

}
