import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.ActorMaterializer;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class PseudoAnonApp {
    public static void main (String[] args) throws InterruptedException, KeeperException, IOException {
        ActorSystem system = ActorSystem.create("anonymizer");
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final 



    }
}
