import akka.actor.Actor;
import akka.actor.ActorRef;
import org.apache.zookeeper.*;

import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class ServerController {

    private static final String SERVERS_PATH = "/servers";
    private static final String SERVERS_PATH_WITH_SLASH = "/servers/";

    private ActorRef storeActor;
    private ZooKeeper zoo;

    public ServerController(ActorRef store, ZooKeeper zoo, String link, String host) throws InterruptedException, KeeperException{
        this.storeActor = store;
        this.zoo = zoo;
        watchChildrenCallback(null);
        String port = link.substring(link.length()-4);
        // The Znode will be deleted upon the client's disconnect
        zoo.create(SERVERS_PATH_WITH_SLASH + link, (host + ":" + port).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);   //Создаем узел (слайд 22)
    }

    private void watchChildrenCallback(WatchedEvent event){
        try{
            /* Получение данных об узлах: getChildren - получение дочерних узлов
             * Каждый сервер регистрируется в zookeeper - создает дочерний узел для узла
             * /servers в содержимом которого хранит свой порт а также подписывается на
             * изменения servers.
             */
            this.storeActor.tell(new AllServersMessage(zoo.getChildren(SERVERS_PATH, this::watchChildrenCallback).stream()
            .map(s -> SERVERS_PATH_WITH_SLASH + s).collect(Collectors.toCollection(ArrayList::new))), ActorRef.noSender());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }

    }

    public void removerWatches() throws KeeperException, InterruptedException{
        zoo.removeAllWatches(SERVERS_PATH, Watcher.WatcherType.Any, true);
    }
}
