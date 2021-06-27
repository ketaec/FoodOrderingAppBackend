package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    /**
     * Customer signup endpoint.
     *
     * @param signupCustomerRequest
     * @return SignupCustomerResponse ResponseEntity
     * @throws SignUpRestrictedException
     */
    @RequestMapping(method = RequestMethod.POST,
            path = "/customer/signup",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(
            @RequestBody final SignupCustomerRequest signupCustomerRequest)
            throws SignUpRestrictedException {
        // validate signupCustomerRequest
        validateSignUpRequest(signupCustomerRequest);

        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());

        final CustomerEntity createdCustomerEntity = customerService.saveCustomer(customerEntity);

        SignupCustomerResponse customerResponse =
                new SignupCustomerResponse()
                        .id(createdCustomerEntity.getUuid())
                        .status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(customerResponse, HttpStatus.CREATED);
    }

    /**
     * Customer login endpoint
     * @param authorization basic username and password
     * @return ResponseEntity SigninResponse
     * @throws AuthenticationFailedException
     */
    @RequestMapping(method = RequestMethod.POST,
            path = "/customer/login",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(
            @RequestHeader("authorization") final String authorization)
            throws AuthenticationFailedException {
        String contactNumber;
        String password;

        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(":");
            contactNumber = decodedArray[0];
            password = decodedArray[1];
        } catch (IllegalArgumentException iae) {
            throw new AuthenticationFailedException("ATH-003",
                    "Incorrect format of decoded customer name and password");
        }


        CustomerAuthEntity customerAuthEntity = customerService.authenticate(contactNumber, password);
        CustomerEntity customer = customerAuthEntity.getCustomer();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setId(customer.getUuid());
        loginResponse.setFirstName(customer.getFirstName());
        loginResponse.setLastName(customer.getLastName());
        loginResponse.setEmailAddress(customer.getEmail());
        loginResponse.setContactNumber(customer.getContactNumber());
        loginResponse.setMessage("LOGGED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", customerAuthEntity.getAccessToken());
        headers.add("access-control-expose-headers", "access-token");
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    private boolean validateSignUpRequest(SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
        if (signupCustomerRequest == null || signupCustomerRequest.getFirstName() == null
                || signupCustomerRequest.getContactNumber() == null
                || signupCustomerRequest.getEmailAddress() == null
                || signupCustomerRequest.getPassword() == null
                || signupCustomerRequest.getFirstName().isEmpty()
                || signupCustomerRequest.getEmailAddress().isEmpty() || signupCustomerRequest.getPassword()
                .isEmpty()
                || signupCustomerRequest.getContactNumber().isEmpty()
        ) {
            throw new SignUpRestrictedException("SGR-005",
                    "Except last name all fields should be filled");
        }
        return true;
    }

}

