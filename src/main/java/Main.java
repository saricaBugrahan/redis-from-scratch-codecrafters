import java.io.IOException;
import java.net.ServerSocket;

public class Main {


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket;
        RedisServerConfiguration redisServerConfiguration = new RedisServerConfiguration(args);
        if (redisServerConfiguration.getRole().equalsIgnoreCase("slave")){
            redisServerConfiguration.getRedisReplicaServer().connectToMaster();
            new Thread(redisServerConfiguration.getRedisReplicaServer()).start();
        }
        RedisTimeoutListener redisTimeoutListener = new RedisTimeoutListener();
        new Thread(redisTimeoutListener).start();
        try{
            serverSocket = new ServerSocket(redisServerConfiguration.getPortNumber());
            serverSocket.setReuseAddress(true);
            while (true){
                new Thread(new RedisClient(serverSocket.accept(),redisServerConfiguration.getRedisRDB())).start();
            }
        }catch (Exception exception){
            System.out.println(exception.getMessage());
        }




    }
}