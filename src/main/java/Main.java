import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {


  public static void main(String[] args){
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        int port = 6379;
        String masterHost;
        String masterPort;
        String role="master";

        for(int i = 0;i< args.length;){
            if(args[i].equalsIgnoreCase("--port")){
                try{
                    port = Integer.parseInt(args[i+1]);
                    i+=2;
                } catch (IndexOutOfBoundsException indexOutOfBoundsException){
                    System.out.println("Port tag used but not entered");
                }
            }
            else if(args[i].equalsIgnoreCase("--replicaof")){
                try {
                    masterHost = args[i+1];
                    masterPort = args[i+2];
                    role = "slave";
                    i+=3;
                } catch (IndexOutOfBoundsException indexOutOfBoundsException){
                    System.out.println("Replicaof tag is used with not appropriate number of arguments");
                }
            }else{
                i+=1;
            }

        }
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            RedisTimeoutListener redisTimeoutListener = new RedisTimeoutListener();
            new Thread(redisTimeoutListener).start();
            while (true){
                clientSocket = serverSocket.accept();
                new Thread(new RedisClient(clientSocket,role)).start();
            }

        } catch (IOException e) {
          System.out.println("IOException: " + e.getMessage());
        }
  }
}
