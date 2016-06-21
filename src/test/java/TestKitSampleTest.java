import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import akka.util.Timeout;
import com.google.protobuf.InvalidProtocolBufferException;
import com.znl.GameMainServer;
import com.znl.define.ActorDefine;
import com.znl.msg.GameMsg;
import com.znl.proto.M1;
import com.znl.proxy.DbProxy;
import com.znl.server.DbServer;
import com.znl.server.PushServer;
import com.znl.service.PlayerService;
import com.znl.service.actor.MySqlActor;
import com.znl.utils.GameUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2015/11/30.
 */
public class TestKitSampleTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem. create() ;
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit. shutdownActorSystem(system) ;
        system = null;
    }

    //@Test
    public void testMysqlQuery(){
     /*   final Props props = MySqlActor.props("192.168.10.190", "game", "root", "AAxx0011!!@@#~~~~~~~~~~~~~~~~~~~~~~~~~");
        final TestActorRef<MySqlActor> ref = TestActorRef. create(system, props, ActorDefine.MYSQL_ACTOR_NAME) ;
        MySqlActor actor = ref.underlyingActor();

        //json_extract(data, '$.id') as id, json_extract(data, '$.accountName') as accountName
        String sql = "select data from Player where json_extract(data, '$.id') = 2200; ";
//        List<JSONObject> queryList = actor.onQuery(sql);
//        queryList.forEach( query -> {
//            System.out.println(query.get("name"));
//        });
//        System.out.println(queryList.toString());*/
    }

//    @Test
    public void testPushServer() {
        final TestActorRef<PushServer> ref = TestActorRef. create(system, Props.create(PushServer.class), ActorDefine.PUSH_SERVER_NAME) ;
        final PushServer actor = ref.underlyingActor();
//        actor.pushMsgToSingeDevice("3789937191499197739", "from server", "hello");
        actor.pushMsgToAllDevice("from server", "all msg");
    }

//    @Test
    public void demonstrateTestActorRef() throws InterruptedException {
        final Props props = DbServer.props("192.168.10.190", 7001, "192.168.10.190", "game", "root", "AAxx0011!!@@#~~~~~~~~~~~~~~~~~~~~~~~~~", null); //203.195.140.103
        final TestActorRef<DbServer> ref = TestActorRef. create(system, props, ActorDefine.DB_SERVER_NAME) ;

        GameMainServer.setSystem(system);

        final DbServer actor = ref. underlyingActor() ;
//        Object obj = actor.getAllPlayerId("9994");
//
//        System.out.println(obj);

        com.google.protobuf.GeneratedMessage msg = M1.M10000.S2C.newBuilder()
                .setRs(1).build();
        System.out.println(msg.toString()) ;
//        actor.saveProtoGeneratedMessage(1000, 1, msg );
        Long id = DbProxy.createProtoGeneratedMessage(1000, msg, 10);

//        byte[] bytes = actor.getProtoGeneratedMessage(1000, 1).get();
        byte[] bytes = DbProxy.getProtoGeneratedMessageBytes(1000, 1);
        try {
            com.google.protobuf.GeneratedMessage msg1 = M1.M10000.S2C.newBuilder()
                    .mergeFrom(bytes).build();

            System.out.println(msg1.toString()) ;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

//        while (true){
//            Thread.sleep(1);
//            Timeout timeout = new Timeout(Duration. create(200, "seconds") ) ;
//            int i = GameUtils.getRandomValueByRange(100);
//            Future<Object> future = Patterns. ask(ref, new GameMsg.GetPlayerByAccountName("xxx" + i + "_9900"), timeout) ;
//            try {
//                Object player = Await. result(future, timeout.duration());
//                System.out.println(player);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

//        final Props sp = Props.create(PlayerService.class);
//        final TestActorRef<PlayerService> playerService = TestActorRef.create(system, sp);

//        playerService.tell(new GameMsg.CreatePlayerActorSuccess(1993819, "xxx9_9993"), ActorRef.noSender());

//        playerService.tell(new GameMsg.GetPlayerSimpleInfo(1993819),  ActorRef.noSender());



//        Object player = GameUtils.futureAsk(ref.actorContext().actorSelection("./"), new GameMsg.GetPlayerByAccountName("asd"), 20);


//        final DbServer actor = ref. underlyingActor() ;
////        assertTrue(actor.isTrue()) ;
//        final Future<Object> future = akka. pattern. Patterns. ask(ref, "say42", 3000) ;
//        assertTrue(future. isCompleted() ) ;
//        assertEquals(42, Await. result(future, Duration.Zero()) ) ;
    }

//    @Test
//    public void testIt() {
//        final Props props = DbServer.props("192.168.10.190", 7001, "192.168.10.190", "game", "root", "AAxx0011!!@@#~~~~~~~~~~~~~~~~~~~~~~~~~");
//        final ActorRef dbServer = system. actorOf(props , ActorDefine.DB_SERVER_NAME) ;
//
//        final JavaTestKit probe = new JavaTestKit(system) ;
//
//        new JavaTestKit.Within(Duration.apply("3 seconds") ) {
//            protected void run() {
//                dbServer.tell("hello", ActorRef.noSender());
//
//                new JavaTestKit.AwaitCond() {
//                    protected boolean cond() {
//                        return probe. msgAvailable() ;
//                    }
//                }
//
//                probe. expectMsgEquals(Duration.Zero(), "hello") ;
//            }
//
//
//        }
//    }
}
