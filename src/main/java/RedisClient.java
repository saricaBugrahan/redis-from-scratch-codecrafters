import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
public class RedisClient implements Runnable{

    static public ConcurrentHashMap<String,String> redisKeyValuePair = new ConcurrentHashMap<>();

    static public ConcurrentHashMap<String,Long[]> redisKeyTimeoutPair = new ConcurrentHashMap<>();

    private final Socket clientSocket;
    private int redisInputCommandCount;
    private ArrayList<String> redisInputPieces;

    private RedisCommandHandler redisCommandHandler;

    public RedisClient(Socket clientSocket){
        this.clientSocket = clientSocket;
        redisCommandHandler = new RedisCommandHandler();
    }
    @Override
    public void run() {
        try{
            BufferedReader dis = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(this.clientSocket.getOutputStream());
            String redisInputString;
            while ((redisInputString = dis.readLine())!= null){
                if (redisInputString.startsWith("*")) {
                    redisInputCommandCount = redisCommandHandler.getCommandLength(redisInputString);
                    redisInputPieces = new ArrayList<>();
                    for (int i = 0; i < redisInputCommandCount * 2; i++) {
                        redisInputString = dis.readLine();
                        if (!redisInputString.startsWith("$")) {
                            redisInputPieces.add(redisInputString);
                        }
                    }
                }
                redisCommandHandler.outputHandler(dos,redisInputPieces);
            }
            dis.close();
            dos.close();
            clientSocket.close();
        }
        catch (IOException e){
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
