import akka.actor.Actor;
import akka.actor.ActorRef;
import org.apache.zookeeper.ZooKeeper;

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
            storeActor.tell(new AllServersMessage(zoo.getChildren("/servers")));
        }catch (Exception ex){
            throw new RuntimeException(ex);

        }

    }
}
