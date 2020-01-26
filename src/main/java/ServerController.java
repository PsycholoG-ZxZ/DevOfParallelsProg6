import akka.actor.Actor;
import akka.actor.ActorRef;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ServerController {
    private ActorRef storeActor;
    private ZooKeeper zoo;

    public ServerController(ActorRef store, ZooKeeper zoo){
        this.storeActor = store;
        this.zoo = zoo;
        watchChildrenCallback();
    }

    public void watchChildrenCallback(){
        try{
            this.storeActor.tell(new AllServersMessage(zoo.getChildren("/servers", this::watchChildrenCallback).stream()
            .map(s -> "/servers " + s).collect(Collectors.toCollection(ArrayList::new))), ActorRef.noSender());
        }catch (Exception ex){
            throw new RuntimeException(ex);

        }

    }
}
