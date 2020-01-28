import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

import java.util.ArrayList;
import java.util.Random;

public class StoreActor extends AbstractActor {
    private ArrayList<String> addresses = new ArrayList<String>();
    private Random randomServer = new Random();


    @Override
    public Receive createReceive(){
        return ReceiveBuilder.create()
                .match(GetMessage.class, m ->
                        getSender().tell(new RandomServerMessage(addresses.get(randomServer.nextInt(addresses.size()))), self()))

                .match(AllServersMessage.class, m ->{ ArrayList<String> newServers = m.getAllServers();
         //               if (addresses.size() == 0) {
         //                   this.addresses.addAll(newServers);
         //               }else{
                            System.out.println("" + m.getAllServers());
                            addresses.clear();
                            addresses.addAll(newServers);
         //               }
                })

                .match(RandomServerMessage.class, m -> this.addresses.remove(m.getServer()))

                .build();

    }

}
