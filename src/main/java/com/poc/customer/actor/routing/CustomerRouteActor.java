package com.poc.customer.actor.routing;

import akka.NotUsed;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.javadsl.Flow;
import akka.http.javadsl.ServerBinding;
import com.poc.customer.valueobject.dataaccess.request.CustomerDataAccessRequestVO;
import com.poc.customer.valueobject.dataaccess.response.CustomerDataAccessResponseVO;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.compat.java8.FutureConverters;
import akka.http.javadsl.marshallers.jackson.Jackson;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class CustomerRouteActor extends AbstractLoggingActor {


    final CompletionStage<ServerBinding> binding;
    Config config=ConfigFactory.load();
    private final ActorRef cassandraDataAccessActor;
    private final ActorRef shardRegionActor;

    public static Props props(ActorRef cassandraDataAccessActor,ActorRef shardRegionActor){
        return Props.create(CustomerRouteActor.class,()-> new CustomerRouteActor(cassandraDataAccessActor,shardRegionActor));
    }

    public CustomerRouteActor(ActorRef cassandraDataAccessActor,ActorRef shardRegionActor) throws ExecutionException, InterruptedException {

        this.cassandraDataAccessActor=cassandraDataAccessActor;
        this.shardRegionActor=shardRegionActor;
        Routes routes=new Routes();
        final Http http = Http.get(getContext().getSystem());
        final ActorMaterializer materializer = ActorMaterializer.create(getContext().getSystem());
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = routes.createRoute().flow(getContext().getSystem(), materializer);
        String hostname=config.getString("akka.akka-http-server-conn.hostname");
        Integer port=Integer.parseInt(config.getString("akka.akka-http-server-conn.port"));
        binding= http.bindAndHandle(routeFlow, ConnectHttp.toHost(hostname,port), materializer);
        log().info("Server online at http://" + hostname + ":" + port);
    }

    public class Routes extends AllDirectives {
        public Route createRoute() throws ExecutionException, InterruptedException {
            return route(
                    getCustomerDetails(),
                    getStatus()
            );
        }

        private Route getCustomerDetails() throws ExecutionException, InterruptedException {
          String routepath= config.getString("akka.routepath.getCustomerDetails");
          final long timeout = config.getLong("akka.servicelayer.timeout");
            /*CustomerDataAccessRequestVO customerDataAccessRequestVO=new CustomerDataAccessRequestVO();
              customerDataAccessRequestVO.setAccountNumber("771026606");
              final CompletionStage<Object> responseCompletionStage = FutureConverters.toJava(Patterns.ask(cassandraDataAccessActor,customerDataAccessRequestVO,timeout));
              responseCompletionStage.thenApply(response -> {
                System.out.println(" response is of type ::"+response.getClass());
                CustomerDataAccessResponseVO customerDataAccessResponseVO=(CustomerDataAccessResponseVO) response;
                return customerDataAccessResponseVO;
            });
            return  route(path(routepath, () -> get(()-> onSuccess(responseCompletionStage, customerDataAccessResponseVO -> complete(StatusCodes.OK,customerDataAccessResponseVO,Jackson.marshaller())))));*/
            return post(() ->
                            path(routepath, () ->
                                    entity(Jackson.unmarshaller(CustomerDataAccessRequestVO.class), customerRequest -> {
                                        log().info("Incoming Request: "+customerRequest.toString());
                                        final CompletionStage<Object> shardingResponseCompletionStage = FutureConverters.toJava(Patterns.ask(shardRegionActor,customerRequest,timeout)).thenCompose(shardRegionResponse ->{
                                            if((null != shardRegionResponse)&& (shardRegionResponse instanceof CustomerDataAccessResponseVO)){
                                                log().info("Fetching cached data for Shard Actor");
                                                return  CompletableFuture.supplyAsync(() ->{
                                                     return shardRegionResponse;
                                                });
                                            }
                                            else{
                                                log().info("Fetching data from Cassandra DataStore, this is first time fetch");
                                                final CompletionStage<Object> responseCompletionStage = FutureConverters.toJava(Patterns.ask(cassandraDataAccessActor,customerRequest,timeout));
                                                 return responseCompletionStage.thenApply(responseobj -> {
                                                     CustomerDataAccessResponseVO customerDataAccessResponseVO = (CustomerDataAccessResponseVO) responseobj;
                                                     shardRegionActor.tell(responseobj,getSelf());
                                                     if(customerDataAccessResponseVO.getAccount_number() !=null){
                                                         return responseobj;
                                                     }
                                                     else{
                                                         return "Not a valid Account Number";
                                                     }
                                                });

                                                }
                                        });

                                        return onSuccess(shardingResponseCompletionStage, customerDataAccessResponseVO -> complete(StatusCodes.OK,customerDataAccessResponseVO,Jackson.marshaller()));
                                    })));
        }

        private Route getStatus() throws ExecutionException, InterruptedException{
            String alivepath = config.getString("akka.routepath.getStatus");
            return  route(path(alivepath, () -> get(()->  complete(StatusCodes.OK,"OK Response From Server"))));

        }
    }


    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class,cmd -> {
                if(cmd.equalsIgnoreCase("Stop")){
                    binding.thenCompose(ServerBinding::unbind)
                            .thenAccept(unbound -> akka.actor.CoordinatedShutdown.get(getContext().getSystem()).run());
                }
                }).build();
    }
}
