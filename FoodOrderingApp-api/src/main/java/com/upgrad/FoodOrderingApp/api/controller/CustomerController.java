package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
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

    @RequestMapping(method = RequestMethod.POST,
            path = "/customer/logout",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        String accessToken;
        try {
            accessToken = authorization.split("Bearer ")[1];
        } catch(ArrayIndexOutOfBoundsException are) {
            accessToken = authorization;
        }

        CustomerAuthEntity customerAuthEntity = customerService.logout(accessToken);

        LogoutResponse logoutResponse = new LogoutResponse()
                .id(customerAuthEntity.getCustomer().getUuid()).message("LOGGED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse, null, HttpStatus.OK);
    }

    /**
     * Customer update endpoint
     *
     * @param authorization token
     * @return ResponseEntity UpdateCustomerResponse
     * @throws UpdateCustomerException or AuthorizationFailedException
     */
    @RequestMapping(method = RequestMethod.PUT,
            path = "/customer",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> update(
            @RequestHeader("authorization") final String authorization,
            @RequestBody final UpdateCustomerRequest updateCustomerRequest)
            throws AuthorizationFailedException, UpdateCustomerException {
        validateUpdateCustomerRequest(updateCustomerRequest.getFirstName());

        String accessToken = authorization.split("Bearer ")[1];

        CustomerEntity customerEntity = customerService.getCustomer(accessToken);
        customerEntity.setFirstName(updateCustomerRequest.getFirstName());
        customerEntity.setLastName(updateCustomerRequest.getLastName());
        CustomerEntity updatedCustomerEntity = customerService.updateCustomer(customerEntity);

        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse();
        updateCustomerResponse.setFirstName(updatedCustomerEntity.getFirstName());
        updateCustomerResponse.setLastName(updatedCustomerEntity.getLastName());
        updateCustomerResponse.setId(updatedCustomerEntity.getUuid());
        updateCustomerResponse.status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse, null, HttpStatus.OK);
    }

    /**
     * Updates the Customer password endpoint
     *
     * @param authorization
     * @param updatePasswordRequest
     * @return ResponseEntity UpdatePasswordResponse
     * @throws UpdateCustomerException and AuthorizationFailedException
     */
    @RequestMapping(method = RequestMethod.PUT,
            path = "/customer/password",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updateCustomerPassword(
            @RequestHeader("authorization") final String authorization,
            @RequestBody UpdatePasswordRequest updatePasswordRequest)
            throws UpdateCustomerException, AuthorizationFailedException {
        String oldPassword = updatePasswordRequest.getOldPassword();
        String newPassword = updatePasswordRequest.getNewPassword();

        if (oldPassword == null || oldPassword == "") {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }
        if (newPassword == null || newPassword == "") {
            throw new UpdateCustomerException("UCR-003", "No field should be empty");
        }

        String accessToken = authorization.split("Bearer ")[1];
        CustomerEntity customerEntityToBeUpdated = customerService.getCustomer(accessToken);
        CustomerEntity updatedCustomerEntity = customerService.updateCustomerPassword(
                oldPassword, newPassword,customerEntityToBeUpdated
        );

        UpdatePasswordResponse updatePasswordResponse = new UpdatePasswordResponse()
                .id(updatedCustomerEntity.getUuid()).status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);

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

    private boolean validateUpdateCustomerRequest(String firstName) throws UpdateCustomerException {
        if (firstName == null || firstName == "") {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }
        return true;
    }

}

