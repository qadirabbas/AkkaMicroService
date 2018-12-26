package com.poc.customer.actor.sharding;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.testkit.TestKit;
import com.poc.customer.actor.database.CassandraDataAccessActor;
import com.poc.customer.actor.routing.CustomerRouteActor;
import com.poc.customer.valueobject.dataaccess.request.CustomerDataAccessRequestVO;
import com.poc.customer.valueobject.dataaccess.response.CustomerDataAccessResponseVO;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import static akka.testkit.JavaTestKit.duration;

public class TestShardingActor {

    private static ActorSystem system;
    //Duration duration=new Duration

    @BeforeClass
    public static void setUp() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static  void tearDown() {
        TestKit.shutdownActorSystem(system, duration("10 second"), false);
        system.terminate();
    }



    @Test
    public void testMockDatabase() {

        new TestKit(system){{

                final ActorRef testMockCassandraActor = system.actorOf(TestCassandraActor.props(), "TestMockCassandraActor");
                CustomerDataAccessRequestVO customerDataAccessRequestVO = new CustomerDataAccessRequestVO();
                customerDataAccessRequestVO.setAccountNumber("12345");
                testMockCassandraActor.tell(customerDataAccessRequestVO, super.testActor());
                expectMsgClass(CustomerDataAccessResponseVO.class);
               // receiveOne(duration("10 second"));
            }};
    }

    @Test
    public void testShard() {

        new TestKit(system){{


            final ActorRef testShardActor = system.actorOf(TestShardActor.props(), "TestShardActor");

            testShardActor.tell("Hello", super.testActor());
            //expectMsgClass(CustomerDataAccessResponseVO.class);
            receiveOne(duration("10 second"));
        }};
    }

    /**
     * Actual Cassandra Database integration test suite
     */
    @Test
    public void testDatabase() {

        new TestKit(system){{

            final ActorRef testCassandraActor = system.actorOf(CassandraDataAccessActor.props(), "TestCassandraActor");
            CustomerDataAccessRequestVO customerDataAccessRequestVO = new CustomerDataAccessRequestVO();
            customerDataAccessRequestVO.setAccountNumber("260026233606");
            testCassandraActor.tell(customerDataAccessRequestVO, super.testActor());
            expectMsgClass(FiniteDuration.apply(10,"second"),CustomerDataAccessResponseVO.class);
        }};
    }

    /**
     * Actual Cassandra Database integration test suite for negative scenario where record does not exists.
     */
    @Test
    public void testDatabaseNegativeScenario() {

        new TestKit(system){{

            final ActorRef testNegativeCassandraActor = system.actorOf(CassandraDataAccessActor.props(), "TestNegativeCassandraActor");
            CustomerDataAccessRequestVO customerDataAccessRequestVO = new CustomerDataAccessRequestVO();
            customerDataAccessRequestVO.setAccountNumber("260026233605");
            testNegativeCassandraActor.tell(customerDataAccessRequestVO, super.testActor());
            expectMsgClass(FiniteDuration.apply(10,"second"),CustomerDataAccessResponseVO.class);
        }};
    }
}
