import java.io.*;
import java.net.Socket;
import java.util.List;

public class RedisReplicaServer {

    private final String masterHost;

    private final int masterPort;

    private final int portNumber;

    private RedisCommandHandler redisCommandHandler;

    RedisReplicaServer(String masterHost, int masterPort,int portNumber){
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.portNumber = portNumber;
        this.redisCommandHandler = new RedisCommandHandler();
    }

    public void connectToMaster() throws IOException {
        Socket masterSocket = new Socket(this.masterHost,this.masterPort);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
        DataOutputStream dataOutputStream = new DataOutputStream(masterSocket.getOutputStream());
        redisCommandHandler.sendResponse(dataOutputStream, List.of("ping"));
        redisCommandHandler.sendResponse(dataOutputStream, List.of(new String[]{"REPLCONF", "listening-port", String.valueOf(portNumber)}));
        redisCommandHandler.sendResponse(dataOutputStream, List.of(new String[]{"REPLCONF", "capa-port", "psync2"}));
        masterSocket.close();
    }



}
