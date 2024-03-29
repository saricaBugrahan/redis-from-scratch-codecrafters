import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RedisReplicaServer implements Runnable {

    private  String masterHost;

    public static int masterPort;

    private  int portNumber;

    private final Socket socket;

    private final RedisCommandHandler redisCommandHandler;

    private final RedisEncoder redisEncoder;

    RedisReplicaServer(Socket socket){

        this.redisCommandHandler = new RedisCommandHandler();
        this.socket = socket;
        this.redisEncoder = new RedisEncoder();
    }

    public void connectToMaster() throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        sendInstructionToMaster(dataOutputStream, List.of("ping"));
        getResponseOfMaster(bufferedReader);
        sendInstructionToMaster(dataOutputStream,List.of(new String[]{"REPLCONF", "listening-port", String.valueOf(RedisServerConfiguration.portNumber)}));
        getResponseOfMaster(bufferedReader);
        sendInstructionToMaster(dataOutputStream,List.of(new String[]{"REPLCONF", "capa","eof","capa", "psync2"}));
        getResponseOfMaster(bufferedReader);
        sendInstructionToMaster(dataOutputStream,List.of(new String[]{"PSYNC", "?", "-1"}));
    }

    public void sendInstructionToMaster(DataOutputStream dataOutputStream, List<String> list) throws IOException {
        redisCommandHandler.sendResponse(dataOutputStream,list);
        dataOutputStream.flush();

    }
    public void getResponseOfMaster(BufferedReader bufferedReader) throws IOException {
        String responseOfMaster;
        ArrayList<String> responseOfMasterArrayList;
        while ((responseOfMaster = bufferedReader.readLine()) != null) {
            if (responseOfMaster.startsWith("*")) {
                int lenResponseOfMaster = redisCommandHandler.getCommandLength(responseOfMaster);
                responseOfMasterArrayList = new ArrayList<>();
                for (int i = 0; i < lenResponseOfMaster * 2; i++) {
                    responseOfMaster = bufferedReader.readLine();
                    if (!responseOfMaster.startsWith("$")) {
                        responseOfMasterArrayList.add(responseOfMaster.toLowerCase());
                    }

                }
                redisCommandHandler.outputHandler(new DataOutputStream(this.socket.getOutputStream()),responseOfMasterArrayList,true);
                RedisCommandHandler.ACK += this.redisEncoder.parseResponseIntoRESPBulk(responseOfMasterArrayList).length();
            } else if (responseOfMaster.equalsIgnoreCase("+PONG")) {
                System.out.println("Response is Pong");
                break;
            } else if (responseOfMaster.equalsIgnoreCase("+OK")) {
                System.out.println("Response is OK");
                break;

            } else if(responseOfMaster.startsWith("+FULL")){
                responseOfMaster = bufferedReader.readLine();
                if (responseOfMaster == null)
                    return;
                int len = Integer.parseInt(responseOfMaster.substring(1));
                char[] rdbFile = new char[len-1];
                bufferedReader.read(rdbFile);
            }


        }

    }

    public void sendInstructionToMaster(ArrayList<String> arrayList) throws IOException {
        DataOutputStream dos = new DataOutputStream(this.socket.getOutputStream());
        this.redisCommandHandler.sendResponse(dos,arrayList);
    }


    @Override
    public void run() {
        while (true){
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                if(bufferedReader.ready()){
                    getResponseOfMaster(bufferedReader);
                }
            } catch (IOException e) {
                try {
                    this.socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}