package com.poc.customer.actor.sharding;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.poc.customer.actor.database.CassandraDataAccessActor;
import com.poc.customer.valueobject.dataaccess.request.CustomerDataAccessRequestVO;
import com.poc.customer.valueobject.dataaccess.response.CustomerDataAccessResponseVO;

public class TestCassandraActor extends AbstractActor {

    public static Props props(){
        return Props.create(TestCassandraActor.class,TestCassandraActor::new);
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerDataAccessRequestVO.class, msg ->{
                    getSender().tell(createResponse(),getSelf());
                }).build();
    }


    public CustomerDataAccessResponseVO createResponse(){

        CustomerDataAccessResponseVO customerDataAccessResponseVO=new CustomerDataAccessResponseVO();
        customerDataAccessResponseVO.setAccount_number("123456");
        customerDataAccessResponseVO.setAccount_type("Test Account Type");
        customerDataAccessResponseVO.setAccount_name("Test Account Name");
        customerDataAccessResponseVO.setAccount_status("Test Account Status");
        customerDataAccessResponseVO.setAccount_subtype("Test Account SubType");

        return customerDataAccessResponseVO;
    }
}
