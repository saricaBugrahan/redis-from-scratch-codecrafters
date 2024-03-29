import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RedisClient implements Runnable{


    static public CopyOnWriteArrayList<RedisReplicaServer> redisReplicaServers = new CopyOnWriteArrayList<>();

    public  static  int RESPOND_REPLICA_COUNTER=0;
    private final Socket clientSocket;

    private ArrayList<String> redisInputPieces;

    private final RedisCommandHandler redisCommandHandler;

    public RedisClient(Socket clientSocket,RedisRDBImpl redisRDB){
        this.clientSocket = clientSocket;
        redisCommandHandler = new RedisCommandHandler();
        redisCommandHandler.setRedisRDB(redisRDB);
        redisRDB.readRDB();
    }
    @Override
    public void run() {
        try{
            BufferedReader dis = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(this.clientSocket.getOutputStream());
            String redisInputString;
            while ((redisInputString = dis.readLine())!= null){
                if (redisInputString.startsWith("*")) {
                    int redisInputCommandCount = redisCommandHandler.getCommandLength(redisInputString);
                    redisInputPieces = new ArrayList<>();
                    for (int i = 0; i < redisInputCommandCount * 2; i++) {
                        redisInputString = dis.readLine();
                        if (!redisInputString.startsWith("$")) {
                            redisInputPieces.add(redisInputString.toLowerCase());
                        }
                    }
                }
                if(redisInputPieces.get(0).equalsIgnoreCase("PSYNC") ){
                    redisReplicaServers.add(new RedisReplicaServer(clientSocket));
                    RedisCommandHandler.noCommandReplicaCounter++;
                }
                if(redisInputPieces.get(0).equalsIgnoreCase("set")){

                    for(RedisReplicaServer replicaServer : redisReplicaServers){
                        replicaServer.sendInstructionToMaster(redisInputPieces);
                    }
                }
                if (redisInputPieces.get(0).equalsIgnoreCase("wait")){
                    System.out.println("Replica Counter set to 0");
                    RESPOND_REPLICA_COUNTER = 0;
                    ArrayList<String> arr = new ArrayList<>();
                    arr.add("REPLCONF");
                    arr.add("GETACK");
                    arr.add("*");
                    for (RedisReplicaServer replicaServer: redisReplicaServers){
                        replicaServer.sendInstructionToMaster(arr);
                    }
                }

                redisCommandHandler.outputHandler(dos,redisInputPieces,false);
            }
            dis.close();
            dos.close();
            clientSocket.close();
        }
        catch (IOException e){
            System.out.println("IOException: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}