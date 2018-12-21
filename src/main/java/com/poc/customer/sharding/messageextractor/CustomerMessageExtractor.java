package com.poc.customer.sharding.messageextractor;

import akka.cluster.sharding.ShardRegion;
import com.poc.customer.valueobject.dataaccess.request.CustomerDataAccessRequestVO;
import com.poc.customer.valueobject.dataaccess.response.CustomerDataAccessResponseVO;

public class CustomerMessageExtractor extends ShardRegion.HashCodeMessageExtractor {

    int noOfShards;
    public CustomerMessageExtractor(int maxNumberOfShards){
        super(maxNumberOfShards);
        noOfShards=10;
    }
    @Override
    public String entityId(Object message) {
        if(message instanceof CustomerDataAccessRequestVO){
            CustomerDataAccessRequestVO customerDataAccessRequestVO=(CustomerDataAccessRequestVO) message;
            String accountNumber=customerDataAccessRequestVO.getAccountNumber();
            return accountNumber;
        }
        else if(message instanceof CustomerDataAccessResponseVO){
            CustomerDataAccessResponseVO customerDataAccessResponseVO=(CustomerDataAccessResponseVO) message;
            String accountNumber=customerDataAccessResponseVO.getAccount_number();
            return  accountNumber;
        }
        else{
            return null;
        }
    }

    @Override
    public String shardId(Object message){

        if(message instanceof CustomerDataAccessRequestVO){
            CustomerDataAccessRequestVO customerDataAccessRequestVO=(CustomerDataAccessRequestVO) message;
            String accountNumber=customerDataAccessRequestVO.getAccountNumber();
            return String.valueOf((Long.parseLong(accountNumber)) % (noOfShards));
        }
        else if(message instanceof CustomerDataAccessResponseVO){
            CustomerDataAccessResponseVO customerDataAccessResponseVO=(CustomerDataAccessResponseVO) message;
            String accountNumber=customerDataAccessResponseVO.getAccount_number();
            return String.valueOf((Long.parseLong(accountNumber)) % (noOfShards));
        }
        else{
            return null;
        }
    }
}
