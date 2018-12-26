package com.poc.customer.actor.sharding;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.poc.customer.valueobject.dataaccess.response.CustomerDataAccessResponseVO;

public class TestShardActor extends AbstractActor  {

    public static Props props(){
        return Props.create(TestShardActor.class,TestShardActor::new);
    }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchAny(o ->{
                        getSender().tell("Default",getSelf());
                    }).build();
        }

}
