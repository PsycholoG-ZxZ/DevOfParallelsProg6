import java.util.ArrayList;

public class AllServersMessage {
    private ArrayList<String> allServers;

    public AllServersMessage(ArrayList<String> allServers){
        this.allServers = allServers;
    }

    public ArrayList<String> getAllServers() {
        return allServers;
    }
}
