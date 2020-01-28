import akka.actor.Actor;
import akka.actor.ActorRef;
import org.apache.zookeeper.*;

import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ServerController {
    private ActorRef storeActor;
    private ZooKeeper zoo;

    public ServerController(ActorRef store, ZooKeeper zoo, String link, String host) throws InterruptedException, KeeperException{
        this.storeActor = store;
        this.zoo = zoo;
        watchChildrenCallback(null);
        String port = link.substring(link.length()-5, link.length()-1);
        zoo.create("/servers" + "/" + link, (host + ":" + port).getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
    }

    private void watchChildrenCallback(WatchedEvent event){
        try{
            this.storeActor.tell(new AllServersMessage(zoo.getChildren("/servers", this::watchChildrenCallback).stream()
            .map(s -> "/servers " + s).collect(Collectors.toCollection(ArrayList::new))), ActorRef.noSender());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

    }

    public void removerWatches(){
        zoo.removeAllWatches("/servers");
    }
}
