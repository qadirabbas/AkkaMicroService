package com.poc.customer.valueobject.dataaccess.request;

import java.io.Serializable;

public class CustomerDataAccessRequestVO implements Serializable {

    private  String accountNumber=null;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "accountNumber : "+accountNumber;
    }
}
