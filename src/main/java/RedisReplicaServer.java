import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
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
        sendAndGetResponseOfMaster(bufferedReader,dataOutputStream, List.of("ping"));
        sendAndGetResponseOfMaster(bufferedReader,dataOutputStream,List.of(new String[]{"REPLCONF", "listening-port", String.valueOf(portNumber)}));
        sendAndGetResponseOfMaster(bufferedReader,dataOutputStream,List.of(new String[]{"REPLCONF", "capa-port", "psync2"}));
        sendAndGetResponseOfMaster(bufferedReader,dataOutputStream,List.of(new String[]{"PSYNC", "?", "-1"}));
        masterSocket.close();
    }

    public List<String> sendAndGetResponseOfMaster(BufferedReader bufferedReader, DataOutputStream dataOutputStream, List<String> list) throws IOException {
        redisCommandHandler.sendResponse(dataOutputStream,list);
        String responseOfMaster;
        ArrayList<String> responseOfMasterArrayList = null;
        while ((responseOfMaster = bufferedReader.readLine())!= null){
            System.out.println(responseOfMaster);
            if (responseOfMaster.startsWith("*")) {
                int lenResponseOfMaster = redisCommandHandler.getCommandLength(responseOfMaster);
                responseOfMasterArrayList = new ArrayList<>();
                for (int i = 0; i < lenResponseOfMaster * 2; i++) {
                    if (!responseOfMaster.startsWith("$")) {
                        responseOfMasterArrayList.add(responseOfMaster);
                    }
                }
            } else if(responseOfMaster.equalsIgnoreCase("+PONG")){
                System.out.println("Response is Pong");
                break;
            } else if (responseOfMaster.equalsIgnoreCase("+OK")){
                System.out.println("Response is OK");
            }
        }
        return responseOfMasterArrayList;
    }





}
