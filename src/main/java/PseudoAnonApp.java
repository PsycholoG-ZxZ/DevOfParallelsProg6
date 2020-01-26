import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.Http;
import akka.stream.ActorMaterializer;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;
import java.util.logging.Logger;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class PseudoAnonApp {
    public static void main (String[] args) throws InterruptedException, KeeperException, IOException {
        ActorSystem system = ActorSystem.create("anonymizer");
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final AsyncHttpClient asyncHttpClient = asyncHttpClient();

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        final Http http = Http.get(system);
        Logger log = Logger.getLogger(PseudoAnonApp.class.getName());
        ZooKeeper zoo = new ZooKeeper("127.0.0.1:2181", 5000, loger -> log.info(loger.toString()));

        ServerController server = ServerController(storeActor, zoo);




    }
}
