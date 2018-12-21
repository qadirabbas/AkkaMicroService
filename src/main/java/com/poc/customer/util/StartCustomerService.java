package com.poc.customer.util;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterShardingSettings;
import com.poc.customer.TestActor;
import com.poc.customer.actor.database.CassandraDataAccessActor;
import com.poc.customer.actor.routing.CustomerRouteActor;
import com.poc.customer.actor.sharding.ShardingActor;
import akka.cluster.sharding.ClusterSharding;
import com.poc.customer.sharding.messageextractor.CustomerMessageExtractor;

public class StartCustomerService {

public static void main(String[] args){
    //create actorSystem
        final ActorSystem system= ActorSystem.create("ServiceActorSystem");
        final ActorRef cassandraDataAccessActor= system.actorOf(CassandraDataAccessActor.props(),"CassandraDataAccessActor");
        final ActorRef shardingActor =system.actorOf(ShardingActor.props(),"ShardingActor");
        final ClusterShardingSettings settings = ClusterShardingSettings.create(system);
        ActorRef sharRegion= ClusterSharding.get(system).start("customer",ShardingActor.props(),settings, new CustomerMessageExtractor(100));
        final ActorRef customerRouteActor= system.actorOf(CustomerRouteActor.props(cassandraDataAccessActor,sharRegion),"CustomerRouteActor");
        customerRouteActor.tell("Entering into Route Actor", ActorRef.noSender());


}
}
