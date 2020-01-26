import akka.actor.ActorRef;
import akka.http.javadsl.server.Route;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import akka.pattern.Patterns;

import static akka.http.javadsl.server.Directives.*;

public class Anonymization {
    private ZooKeeper zoo;
    private AsyncHttpClient asyncHttp;
    private ActorRef storage;

    public Anonymization(ZooKeeper zoo, AsyncHttpClient http, ActorRef st){
        this.zoo = zoo;
        this.asyncHttp = http;
        this.storage = st;
    }

    public Route routeCreater(){
        Route route = get(()-> parameter("url", url ->
                parameter("count", count ->
                {
                    int count_int = Integer.parseInt(count);
                    CompletionStage<Response> response;
                    if (count_int == 0){
                        response = fetch(asyncHttp.prepareGet(url).build());
                    }else{
                        response = requestTreatment(url);
                    }
                    return completeOKWithFutureString(response.thenApply(Response::getResponseBody));

                }))
        );

    }

    private CompletionStage<Response> requestTreatment(String url) {
        return Patterns.ask(storage, new GetMessage(), Duration.ofSeconds(5)).thenApply()
    }

    private CompletionStage<Response> fetch(Request build) {
        return asyncHttp.executeRequest(build).toCompletableFuture();
    }
}
