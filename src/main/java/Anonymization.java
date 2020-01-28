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

    //Строим дерево ROUTE  и задаем обработчик запросов
    public Route routeCreater(){
        Route route = get(()-> parameter(URL, url ->
                parameter(COUNT, count ->
                {
                    /*
                     * Разрабатываем akka http сервер который при получении запроса либо
                     * отправляет его на случайный сервер, уменьшая счетчик на 1 Либо
                     * осуществляет get для данного url и возвращает.
                     *
                     * count & count_int содержат одинаковое значение счетчика
                     * различие ток в типе данных (String, int соответственно)
                     */
                    int count_int = Integer.parseInt(count);
                    CompletionStage<Response> response;
                    if (count_int == 0){
                        response = fetch(asyncHttp.prepareGet(url).build()); // Если счетчик равен 0, то осуществляем запрос по URL, получаем данные по этому URL
                    }else{

                        /* Если счетчик не равен 0, то получаем новый URL от хранилища и делаем запрос к нему с аналогичными параметрами
                         * URL и COUNT, только COUNT на 1 меньше.
                         */
                        response = requestTreatment(url, count_int-1);
                    }
                    return completeOKWithFutureString(response.thenApply(Response::getResponseBody));

                }))
        );
        return route;
    }

    // Обработка запроса с понижением COUNT
    private CompletionStage<Response> requestTreatment(String url, int count) {
        return Patterns.ask(storage, new GetMessage(), Duration.ofSeconds(5)).thenApply(s -> ((RandomServerMessage)s).getServer()) //забираем рандомный сервер
                .thenCompose(m -> asyncHttp.executeRequest(getRequest(getNewUrl(m),url,count)).toCompletableFuture() // сервер совершает запрос с аналогичными параметрами с уменьшением на 1
                .handle((res, ex) -> {
                    storage.tell(new RandomServerMessage(m),ActorRef.noSender());
                    return res;
                }));
    }

    //формируем новые данные для нового реквеста
    private String getNewUrl(String m) {
        try{
            String serverUrl = new String(zoo.getData(m, false,null));
            return serverUrl;
        }catch (InterruptedException | KeeperException ex){
            throw new RuntimeException(ex);
        }
    }

    //Непосредственное формирование нового запроса
    private Request getRequest(String servUrl, String url, int count) {
        String count_str = Integer.toString(count);
        return asyncHttp.prepareGet(servUrl).addQueryParam(URL, url).addQueryParam(COUNT, count_str).build();

    }

    private CompletionStage<Response> fetch(Request req) {
        System.out.println("REQUEST: " + req.getUri());
        return asyncHttp.executeRequest(req).toCompletableFuture();
    }
}
