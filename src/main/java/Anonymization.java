import akka.actor.ActorRef;
import akka.http.javadsl.server.Route;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import akka.pattern.Patterns;

import static akka.http.javadsl.server.Directives.*;

public class Anonymization {

    private static final String URL = "url";
    private static final String COUNT = "count";

    private ZooKeeper zoo;
    private AsyncHttpClient asyncHttp;
    private ActorRef storage;

    public Anonymization(ZooKeeper zoo, AsyncHttpClient http, ActorRef st){
        this.zoo = zoo;
        this.asyncHttp = http;
        this.storage = st;
    }

    public Route routeCreater(){
        Route route = get(()-> parameter(URL, url ->
                parameter(COUNT, count ->
                {
                    int count_int = Integer.parseInt(count);
                    CompletionStage<Response> response;
                    if (count_int == 0){
                        response = fetch(asyncHttp.prepareGet(url).build());
                    }else{
                        response = requestTreatment(url, count_int-1);
                    }
                    return completeOKWithFutureString(response.thenApply(Response::getResponseBody));

                }))
        );
        return route;
    }

    private CompletionStage<Response> requestTreatment(String url, int count) {
        return Patterns.ask(storage, new GetMessage(), Duration.ofSeconds(5)).thenApply(s -> ((RandomServerMessage)s).getServer())
                .thenCompose(m -> asyncHttp.executeRequest(getRequest(getNewUrl(m),url,count)).toCompletableFuture()
                .handle((res, ex) -> {
                    storage.tell(new RandomServerMessage(m),ActorRef.noSender());
                    return res;
                }));
    }

    private String getNewUrl(String m) {
        try{
            String serverUrl = new String(zoo.getData(m, false,null));
            return serverUrl;
        }catch (InterruptedException | KeeperException ex){
            throw new RuntimeException(ex);
        }
    }

    private Request getRequest(String servUrl, String url, int count) {
        String count_str = Integer.toString(count);
        return asyncHttp.prepareGet(servUrl).addQueryParam("url", url).addQueryParam("count", count_str).build();

    }

    private CompletionStage<Response> fetch(Request build) {
        return asyncHttp.executeRequest(build).toCompletableFuture();
    }
}
