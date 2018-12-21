package com.poc.customer.valueobject.dataaccess.response;

import com.datastax.driver.mapping.annotations.Table;

import java.io.Serializable;

@Table(keyspace = "oneviewdashboarddev02", name = "billing_account_by_account")
public class CustomerDataAccessResponseVO implements Serializable {

    String account_number;
    String account_name;
    String account_status;
    String account_subtype;
    String account_type;

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public String getAccount_status() {
        return account_status;
    }

    public void setAccount_status(String account_status) {
        this.account_status = account_status;
    }

    public String getAccount_subtype() {
        return account_subtype;
    }

    public void setAccount_subtype(String account_subtype) {
        this.account_subtype = account_subtype;
    }

    public String getAccount_type() {
        return account_type;
    }

    public void setAccount_type(String account_type) {
        this.account_type = account_type;
    }
}
