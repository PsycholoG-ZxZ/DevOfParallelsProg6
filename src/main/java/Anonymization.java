import akka.http.javadsl.server.Route;
import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Response;

import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.parameter;

public class Anonymization {
    private ZooKeeper zoo;
    private AsyncHttpClient asyncHttp;

    public Anonymization(ZooKeeper zoo, AsyncHttpClient http){
        this.zoo = zoo;
        this.asyncHttp = http;
    }

    public Route routeCreater(){
        Route route = get(()-> parameter("url", url ->
                parameter("count", count ->
                {
                    int count_int = Integer.parseInt(count);
                    CompletionStage<Response> response;
                    if (count_int == 0){
                        response = fetch ()
                    }

                }))
        );

    }
}
