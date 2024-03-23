import java.io.*;
import java.net.Socket;
import java.util.List;

public class RedisReplicaServer {

    private final String masterHost;

    private final int masterPort;

    private RedisCommandHandler redisCommandHandler;

    RedisReplicaServer(String masterHost, int masterPort){
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.redisCommandHandler = new RedisCommandHandler();
    }

    public void connectToMaster() throws IOException {
        Socket masterSocket = new Socket(this.masterHost,this.masterPort);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
        DataOutputStream dataOutputStream = new DataOutputStream(masterSocket.getOutputStream());
        redisCommandHandler.sendResponse(dataOutputStream, List.of("ping"));
    }



}
