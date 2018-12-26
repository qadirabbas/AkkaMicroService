package com.poc.customer.actor.database;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.pattern.PatternsCS;
import com.datastax.driver.core.*;
import com.datastax.driver.mapping.*;
import com.poc.customer.valueobject.dataaccess.request.CustomerDataAccessRequestVO;
import com.poc.customer.valueobject.dataaccess.response.CustomerDataAccessResponseVO;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CassandraDataAccessActor extends AbstractLoggingActor {

    private  Cluster cluster=null;
    private  Session session=null;
    private MappingManager mappingManager=null;


    public static Props props(){
        return Props.create(CassandraDataAccessActor.class,CassandraDataAccessActor::new);
    }

    public CassandraDataAccessActor(){
        Config config= ConfigFactory.load();
        try {
            cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9042).withAuthProvider(new PlainTextAuthProvider("cassandra", "cassandra")).build();
            String keyspace=config.getString("akka.cassandra-keyspace.name");
            session = cluster.connect(keyspace);
            mappingManager=new MappingManager(session);

        }
        catch(Exception e){
            log().error(e.getMessage());
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerDataAccessRequestVO.class, msg ->{
                    CustomerDetails(msg.getAccountNumber());
                }).build();
    }

    public void CustomerDetails(String accountNumber){


            String Query = "select * from billing_account_by_account where account_number = '"+accountNumber+"';";
            //String Query = "select * from billing_account_by_account limit 5;";
            //CompletableFuture.supplyAsync will convert java object into CompletionStage<Object> format
            final CompletionStage<CustomerDataAccessResponseVO> completionStageResult=CompletableFuture.supplyAsync(() ->{
            ResultSet resultSet=session.execute(Query);
            Row row=resultSet.one();
            if(null != row) {
                log().info("Inside CassandraDataAccessActor:: " + row.toString());
                CustomerDataAccessResponseVO customerDataAccessResponseVO = new CustomerDataAccessResponseVO();
                customerDataAccessResponseVO.setAccount_number(row.getString("account_number"));
                customerDataAccessResponseVO.setAccount_type(row.getString("account_type"));
                customerDataAccessResponseVO.setAccount_name(row.getString("account_name"));
                customerDataAccessResponseVO.setAccount_status(row.getString("account_status"));
                customerDataAccessResponseVO.setAccount_subtype(row.getString("account_subtype"));

                return customerDataAccessResponseVO;
            }
            else{
                return new CustomerDataAccessResponseVO();
            }
        });
        PatternsCS.pipe(completionStageResult, context().dispatcher()).to(sender());
    }
}
