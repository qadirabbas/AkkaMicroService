package com.poc.customer.actor.sharding;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.pattern.PatternsCS;
import com.poc.customer.valueobject.dataaccess.request.CustomerDataAccessRequestVO;
import com.poc.customer.valueobject.dataaccess.response.CustomerDataAccessResponseVO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ShardingActor extends AbstractLoggingActor {

    private  final CustomerDataAccessResponseVO customerDataAccessResponseVO=new CustomerDataAccessResponseVO();

    public static Props props (){
        return Props.create(ShardingActor.class,ShardingActor::new);
    }
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerDataAccessRequestVO.class, requestmessage -> {
                    log().info("Account Number for cached object: " + customerDataAccessResponseVO.getAccount_number());
                if(null !=customerDataAccessResponseVO.getAccount_number()){
                    final CompletionStage<CustomerDataAccessResponseVO> completionStageResult= CompletableFuture.supplyAsync(() ->{
                        return customerDataAccessResponseVO;
                            });
                    PatternsCS.pipe(completionStageResult, context().dispatcher()).to(getSender());
                }
                else{
                    log().info("Response message is null!!. Cache not set");
                   final CompletionStage<String> completionStageResult= CompletableFuture.supplyAsync(() ->{
                        return "CacheNotSet";
                    });
                    PatternsCS.pipe(completionStageResult, context().dispatcher()).to(getSender());
                }
        }).match(CustomerDataAccessResponseVO.class, responseMessage -> {
                    log().info("Setting Cache object locally in ShardActor");
                            customerDataAccessResponseVO.setAccount_number(responseMessage.getAccount_number());
                            customerDataAccessResponseVO.setAccount_name(responseMessage.getAccount_name());
                            customerDataAccessResponseVO.setAccount_type(responseMessage.getAccount_type());
                            customerDataAccessResponseVO.setAccount_subtype(responseMessage.getAccount_subtype());
                            customerDataAccessResponseVO.setAccount_status(responseMessage.getAccount_status());

                    }).build();
    }
}
