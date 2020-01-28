import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

import java.util.ArrayList;
import java.util.Random;

public class StoreActor extends AbstractActor {
    private ArrayList<String> addresses = new ArrayList<>();
    private Random randomServer = new Random();


    @Override
    public Receive createReceive(){
        return ReceiveBuilder.create()
                .match(GetMessage.class, m ->
                        getSender().tell(new RandomServerMessage(addresses.get(randomServer.nextInt(addresses.size()))), self()))

                .match(AllServersMessage.class, m ->{
                    ArrayList<String> newServers = m.getAllServers();

                    /*
                     * Каждый клиент поддерживает сессию – отправляет heartbeat
                     * Получаем список подключенных в данный момент клиентов
                     * (17 слайд)
                     */

                    System.out.println("HEARTBEAT: " + m.getAllServers());
                    addresses.clear();
                    addresses.addAll(newServers);
                })

                .match(RandomServerMessage.class, m -> this.addresses.remove(m.getServer()))

                .build();

    }

}
