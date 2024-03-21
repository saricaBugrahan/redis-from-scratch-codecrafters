import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class RedisClient implements Runnable{

    static public ConcurrentHashMap<String,String> redisKeyValuePair = new ConcurrentHashMap();
    private Socket clientSocket;
    private int redisInputCommandCount;

    private ArrayList<String> redisInputPieces;

    public RedisClient(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    private int getCommandLength(String command){
        return Integer.parseInt(command.substring(1).replace("\r\n",""));
    }

    private String parseResponseIntoRESPBulk(String response){
        return "$"+response.length()+"\r\n"+response+"\r\n";
    }

    @Override
    public void run() {
        try{
            BufferedReader dis = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(this.clientSocket.getOutputStream());
            String redisInputString;
            while ((redisInputString = dis.readLine())!= null){
                if (redisInputString.startsWith("*")) {
                    redisInputCommandCount = getCommandLength(redisInputString);
                    redisInputPieces = new ArrayList<>();
                    for (int i = 0; i < redisInputCommandCount * 2; i++) {
                        redisInputString = dis.readLine();
                        if (!redisInputString.startsWith("$")) {
                            redisInputPieces.add(redisInputString);
                        }
                    }
                }
                redisInputPieces.forEach(System.out::println);
                int i = 0;
                while(i<redisInputPieces.size()){
                    String redisCommand = redisInputPieces.get(i);
                    if (redisCommand.equalsIgnoreCase("ping")) {
                        dos.write("+PONG\r\n".getBytes(StandardCharsets.UTF_8));
                        i++;
                    } else if (redisCommand.equalsIgnoreCase("echo")) {
                        String response = parseResponseIntoRESPBulk(redisInputPieces.get(i+1));

                        dos.write(response.getBytes(StandardCharsets.UTF_8));
                        i+=2;
                    } else if (redisCommand.equalsIgnoreCase("set")) {
                        redisKeyValuePair.put(redisInputPieces.get(i+1), redisInputPieces.get(i+2));
                        String response = parseResponseIntoRESPBulk("OK");
                        dos.write(response.getBytes(StandardCharsets.UTF_8));
                        System.out.println("Enter the set");

                        i+=3;
                    } else if (redisCommand.equalsIgnoreCase("get")) {
                        String response = parseResponseIntoRESPBulk(redisKeyValuePair.get(redisInputPieces.get(i+1)));
                        dos.write(response.getBytes(StandardCharsets.UTF_8));
                        i+=2;
                        System.out.println("Enters to get");
                    }else{
                        System.out.println("Invalid Case");
                    }
                }
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
