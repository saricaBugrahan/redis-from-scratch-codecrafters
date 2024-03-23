import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisReplicaServer {

    private  String masterHost;

    private  int masterPort;

    private  int portNumber;

    private  Socket socket;


    private RedisCommandHandler redisCommandHandler;

    RedisReplicaServer(String masterHost, int masterPort,int portNumber){
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.portNumber = portNumber;
        this.redisCommandHandler = new RedisCommandHandler();
        this.socket = null;
    }

    RedisReplicaServer(Socket socket){

        this.redisCommandHandler = new RedisCommandHandler();
        this.socket = socket;
    }

    public void connectToMaster() throws IOException {
        Socket masterSocket = new Socket(this.masterHost,this.masterPort);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(masterSocket.getInputStream()));
        DataOutputStream dataOutputStream = new DataOutputStream(masterSocket.getOutputStream());
        sendCommand(dataOutputStream, List.of("ping"));
        getResponseOfMaster(bufferedReader);
        sendCommand(dataOutputStream,List.of(new String[]{"REPLCONF", "listening-port", String.valueOf(portNumber)}));
        getResponseOfMaster(bufferedReader);
        sendCommand(dataOutputStream,List.of(new String[]{"REPLCONF", "capa-port", "psync2"}));
        getResponseOfMaster(bufferedReader);
        sendCommand(dataOutputStream,List.of(new String[]{"PSYNC", "?", "-1"}));
        getResponseOfMaster(bufferedReader);
        masterSocket.close();
    }

    public void sendCommand(DataOutputStream dataOutputStream, List<String> list) throws IOException {
        redisCommandHandler.sendResponse(dataOutputStream,list);
    }

    public List<String> getResponseOfMaster(BufferedReader bufferedReader) throws IOException {
        String responseOfMaster;
        ArrayList<String> responseOfMasterArrayList = new ArrayList<>();
        while ((responseOfMaster = bufferedReader.readLine())!= null){
            if (responseOfMaster.startsWith("*")) {
                int lenResponseOfMaster = redisCommandHandler.getCommandLength(responseOfMaster);
                responseOfMasterArrayList = new ArrayList<>();
                for (int i = 0; i < lenResponseOfMaster * 2; i++) {
                    if (!responseOfMaster.startsWith("$")) {
                        responseOfMasterArrayList.add(responseOfMaster.toLowerCase());
                    }
                }
            } else if(responseOfMaster.equalsIgnoreCase("+PONG")){
                System.out.println("Response is Pong");
                break;
            } else if (responseOfMaster.equalsIgnoreCase("+OK")){
                System.out.println("Response is OK");
                break;
            }
        }
        return responseOfMasterArrayList;
    }

    public void sendCommand(ArrayList<String> arrayList) throws IOException {

        System.out.println("Send Command");
        DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
        this.redisCommandHandler.sendResponse(dos,arrayList);


    }





}
