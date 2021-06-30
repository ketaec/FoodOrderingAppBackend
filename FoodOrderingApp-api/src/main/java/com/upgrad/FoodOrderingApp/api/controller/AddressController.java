package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/")
public class AddressController {
    @Autowired
    CustomerService customerService;

    @Autowired
    AddressService addressService;

    @RequestMapping(method = RequestMethod.POST,
            path = "/address",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(
            @RequestHeader("authorization") final String authorization,
            @RequestBody(required = false) SaveAddressRequest saveAddressRequest)
            throws AuthorizationFailedException, AddressNotFoundException, SaveAddressException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        AddressEntity addressEntity = new AddressEntity();
        addressEntity.setFlatBuilNo(saveAddressRequest.getFlatBuildingName());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setUuid(UUID.randomUUID().toString());

        StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());

        AddressEntity savedAddress = addressService.saveAddress(addressEntity,stateEntity);

        CustomerAddressEntity customerAddressEntity = addressService.saveCustomerAddressEntity(customerEntity,savedAddress);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse()
                .id(savedAddress.getUuid())
                .status("ADDRESS SUCCESSFULLY REGISTERED");

        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.GET,
            path = "/address/customer",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllSavedAddress(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntity = customerService.getCustomer(accessToken);   // all auth exceptions are handled here

        List<AddressEntity> addressEntities = addressService.getAllAddress(customerEntity);
        Collections.reverse(addressEntities);

        List<AddressList> addressLists = new LinkedList<>();
        addressEntities.forEach(addressEntity -> {
                AddressListState addressListState = new AddressListState()
                        .stateName(addressEntity.getState().getStateName())
                        .id(UUID.fromString(addressEntity.getState().getStateUuid()));
                AddressList addressList = new AddressList()
                        .id(UUID.fromString(addressEntity.getUuid()))
                        .city(addressEntity.getCity())
                        .flatBuildingName(addressEntity.getFlatBuilNo())
                        .locality(addressEntity.getLocality())
                        .pincode(addressEntity.getPincode())
                        .state(addressListState);
                addressLists.add(addressList);
        });

        AddressListResponse addressListResponse = new AddressListResponse().addresses(addressLists);
        return new ResponseEntity<AddressListResponse>(addressListResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            path = "/address/{address_id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteSavedAddress(
            @RequestHeader("authorization")final String authorization,
            @PathVariable("address_id")final String addressId)
            throws AuthorizationFailedException, AddressNotFoundException {
        String accessToken = authorization.split("Bearer ")[1];

        CustomerEntity customerEntity = customerService.getCustomer(accessToken);

        final AddressEntity addressToBeDeleted = addressService.getAddressByUUID(addressId, customerEntity);
        final AddressEntity deletedAddress = addressService.deleteAddress(addressToBeDeleted);

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse()
                .id(UUID.fromString(deletedAddress.getUuid()))
                .status("ADDRESS DELETED SUCCESSFULLY");

        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse,HttpStatus.OK);
    }
}
