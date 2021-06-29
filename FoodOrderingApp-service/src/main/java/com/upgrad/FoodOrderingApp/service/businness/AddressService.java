package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAuthDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AddressService {
    @Autowired
    AddressDao addressDao;

    @Autowired
    CustomerAuthDao customerAuthDao;

    @Autowired
    StateDao stateDao;

    @Autowired
    CustomerAddressDao customerAddressDao;

    public StateEntity getStateByUUID(final String stateUuid)
            throws AddressNotFoundException, SaveAddressException {
        if (stateUuid == null || stateUuid.isEmpty()) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }

        final StateEntity stateEntity = stateDao.getStateByUuid(stateUuid);

        if (null == stateEntity) {
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }
        return stateEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity, StateEntity stateEntity)throws SaveAddressException {

        if (addressEntity.getCity() == null || addressEntity.getCity().trim().isEmpty()
                || addressEntity.getFlatBuilNo() == null || addressEntity.getFlatBuilNo().trim().isEmpty()
                || addressEntity.getPincode() == null || addressEntity.getPincode().trim().isEmpty()
                || addressEntity.getLocality() == null || addressEntity.getLocality().trim().isEmpty()) {
            throw new SaveAddressException("SAR-001","No field can be empty");
        }

        if(!isPincodeValid(addressEntity.getPincode())){
            throw new SaveAddressException("SAR-002","Invalid pincode");
        }

        addressEntity.setState(stateEntity);
        AddressEntity savedAddress = addressDao.saveAddress(addressEntity);
        return savedAddress;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity saveCustomerAddressEntity(CustomerEntity customerEntity, AddressEntity addressEntity){

        //Creating new CustomerAddressEntity and setting the data.
        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setCustomer(customerEntity);
        customerAddressEntity.setAddress(addressEntity);

        //Saving the newly Created CustomerAddressEntity in the DB.
        CustomerAddressEntity createdCustomerAddressEntity = customerAddressDao.saveCustomerAddress(customerAddressEntity);
        return createdCustomerAddressEntity;

    }

    public boolean isPincodeValid(String pincode){
        Pattern p = Pattern.compile("\\d{6}\\b");
        Matcher m = p.matcher(pincode);
        return (m.find() && m.group().equals(pincode));
    }
}
