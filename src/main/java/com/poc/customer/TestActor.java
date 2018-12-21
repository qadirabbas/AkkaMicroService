package com.poc.customer;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

public class TestActor extends AbstractLoggingActor {

    public static Props props(){
        return Props.create(TestActor.class,TestActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg ->{
        System.out.println("message received:: "+msg);
        }).build();
    }
}
