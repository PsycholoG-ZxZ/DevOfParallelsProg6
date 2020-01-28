import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import static org.asynchttpclient.Dsl.asyncHttpClient;

public class PseudoAnonApp {

    private static final int HOST = 0;
    private static final int PORT = 1;
    private static final String HTTP = "http://";
    private static final String IP_PORT_ZOO = "127.0.0.1:2181";
    private static final String LOCALHOST = "localhost";

    /*
    * Требуется разработать приложение использующее технологии zookeeper, акка
    * и позволяющее «анонимизировать» запрос.
    *
    */

    public static void main (String[] args) throws InterruptedException, KeeperException, IOException {
        ActorSystem system = ActorSystem.create("anonymizer");
        ActorRef storeActor = system.actorOf(Props.create(StoreActor.class));
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final AsyncHttpClient asyncHttpClient = asyncHttpClient();

        /*
        *   Администратор запускает несколько серверов. В параметре командной строки он указывает порт для каждого.
         */
        String host = HTTP + args[HOST];
        int port = Integer.parseInt(args[PORT]);

        final Http http = Http.get(system);
        Logger log = Logger.getLogger(PseudoAnonApp.class.getName());

        /* Создаем экземпляр класса ZooKeeper
        *  */

        ZooKeeper zoo = new ZooKeeper(IP_PORT_ZOO, 5000, loger -> log.info(loger.toString()));



        String link = LOCALHOST + port;

        ServerController server = new ServerController(storeActor, zoo, link, host);

        final Anonymization anonServer = new Anonymization(zoo,asyncHttpClient, storeActor);
        final Flow<HttpRequest, HttpResponse, NotUsed> flowForServer = anonServer.routeCreater().flow(system,materializer);
        final CompletionStage<ServerBinding> bind = http.bindAndHandle(flowForServer, ConnectHttp.toHost(host,port), materializer);

        System.in.read();
        asyncHttpClient.close();
        server.removerWatches();
        zoo.close();
        bind.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate());




    }
}
