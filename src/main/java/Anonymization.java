import org.apache.zookeeper.ZooKeeper;
import org.asynchttpclient.AsyncHttpClient;

public class Anonymization {
    private ZooKeeper zoo;
    private AsyncHttpClient asyncHttp;

    public Anonymization(ZooKeeper zoo, AsyncHttpClient http){
        this.zoo = zoo;
        this.asyncHttp = http;
    }
}
