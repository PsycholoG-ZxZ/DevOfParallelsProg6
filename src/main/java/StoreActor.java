import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

import java.util.ArrayList;
import java.util.Random;

public class StoreActor extends AbstractActor {
    private ArrayList<String> addresses = new ArrayList<>();
    private Random randomServer = new Random();


    @Override
    public Receive createReceive(){
        /* Создаем хранилище конфигурации
         * Принимает 3 команды
         */
        return ReceiveBuilder.create()
                /* Запос на получение случайного сервера */
                .match(GetMessage.class, m ->
                        getSender().tell(new RandomServerMessage(addresses.get(randomServer.nextInt(addresses.size()))), self()))
                /* Вывод списка серверов (который отправит zoo watcher) */
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
                /* В случае прерывания сессии, подписки исчезают. Все неактивное убирается (слайд 19)
                 * Для процесса удаления и вывода рандомного сервера использовался один и тот же класс RandomServerMessage
                 * из-за того, что нам необходимы только данные по серверам, соответственно класс для этих процессов будет индентичными
                 * поэтому принято решение не создавать сторонний повторяющийся класс.
                 */
                .match(RandomServerMessage.class, m -> this.addresses.remove(m.getServer()))
                .build();
    }

}
